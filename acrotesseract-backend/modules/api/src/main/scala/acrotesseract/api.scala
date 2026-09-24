package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

/** A transport-neutral HTTP request, so the same router serves Lambda and the local server. */
final case class Request(method: String, path: String, body: Option[String] = None)

final case class Response(status: Int, body: String, headers: Map[String, String] = Map.empty)

object Response:
  private val jsonHeaders = Map("Content-Type" -> "application/json")

  def json[A](status: Int, value: A)(using JsonValueCodec[A]): Response =
    Response(status, writeToString(value), jsonHeaders)

  def error(status: Int, message: String): Response = json(status, ErrorBody(message))

final case class Health(status: String, stage: String)
final case class ErrorBody(error: String)

given JsonValueCodec[Health] = JsonCodecMaker.make
given JsonValueCodec[ErrorBody] = JsonCodecMaker.make
given JsonValueCodec[List[Pose]] = JsonCodecMaker.make

/** Read access to poses. The DynamoDB implementation comes with the store module. */
trait PoseRepository:
  def listPoses(): List[Pose]

final class InMemoryPoseRepository(poses: List[Pose]) extends PoseRepository:
  def listPoses(): List[Pose] = poses.sortBy(_.name.toLowerCase)

/** Routes requests to handlers with a plain pattern match, keeping the Lambda startup path short. */
final class Router(stage: String, poses: PoseRepository):
  def handle(req: Request): Response =
    (req.method, segments(req.path)) match
      case ("GET", List("api", "health")) => Response.json(200, Health("ok", stage))
      case ("GET", List("api", "poses"))  => Response.json(200, poses.listPoses())
      case (_, "api" :: _)                => Response.error(404, s"No route for ${req.method} ${req.path}")
      case _                              => Response.error(404, "Not found")

  private def segments(path: String): List[String] =
    path.split('/').iterator.filter(_.nonEmpty).toList

object Router:
  /** Builds the router from environment variables set by CDK (or by sbt for local runs). */
  def fromEnv(): Router =
    val stage = sys.env.getOrElse("STAGE", "local")
    new Router(stage, new InMemoryPoseRepository(Nil))
