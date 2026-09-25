package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*

import java.util.UUID
import scala.util.control.NonFatal

/** Routes requests to handlers with a plain pattern match, keeping the Lambda startup path short.
  *
  * @param writesEnabled
  *   POST/PUT are refused with 403 when false. Deployed stages keep writes off until Google sign-in exists.
  */
final class Router(stage: String, repo: AcroRepository, writesEnabled: Boolean = false):
  def handle(req: Request): Response =
    try route(req)
    catch
      case NonFatal(e) =>
        System.err.println(s"Unhandled error for ${req.method} ${req.path}: $e")
        e.printStackTrace()
        Response.error(500, "Internal server error")

  private def route(req: Request): Response =
    (req.method, segments(req.path)) match
      case ("GET", List("api", "health"))          => Response.json(200, Health("ok", stage))
      case ("GET", List("api", "poses"))           => Response.json(200, repo.listPoses())
      case ("GET", List("api", "poses", id))       => withId(id)(getPose)
      case ("GET", List("api", "transitions"))     => Response.json(200, repo.listTransitions())
      case ("GET", List("api", "transitions", id)) => withId(id)(getTransition)
      case ("POST", List("api", "poses"))          => write(req)(createPose)
      case ("PUT", List("api", "poses", id))       => write(req)(r => withId(id)(updatePose(r, _)))
      case ("POST", List("api", "transitions"))    => write(req)(createTransition)
      case ("PUT", List("api", "transitions", id)) => write(req)(r => withId(id)(updateTransition(r, _)))
      case (_, "api" :: _)                         => Response.error(404, s"No route for ${req.method} ${req.path}")
      case _                                       => Response.error(404, "Not found")

  private def getPose(id: UUID): Response =
    repo.getPose(id) match
      case Some(pose) =>
        Response.json(200, PoseDetail(pose, repo.transitionsFrom(id), repo.transitionsTo(id)))
      case None => Response.error(404, s"Pose $id not found")

  private def getTransition(id: UUID): Response =
    val detail = for
      t <- repo.getTransition(id)
      from <- repo.getPose(t.poseFrom)
      to <- repo.getPose(t.poseTo)
    yield TransitionDetail(t, from, to)
    detail match
      case Some(d) => Response.json(200, d)
      case None    => Response.error(404, s"Transition $id not found")

  private def createPose(req: Request): Response =
    result(parse[PoseBody](req).flatMap(Validation.pose).flatMap(repo.createPose)) { pose =>
      Response.json(201, pose, Map("Location" -> s"/api/poses/${pose.id}"))
    }

  private def updatePose(req: Request, id: UUID): Response =
    val updated = for
      body <- parse[PoseBody](req)
      version <- requireVersion(body.version)
      input <- Validation.pose(body)
      pose <- repo.updatePose(id, input, version)
    yield pose
    result(updated)(Response.json(200, _))

  private def createTransition(req: Request): Response =
    result(parse[TransitionBody](req).flatMap(Validation.transition).flatMap(repo.createTransition)) { t =>
      Response.json(201, t, Map("Location" -> s"/api/transitions/${t.id}"))
    }

  private def updateTransition(req: Request, id: UUID): Response =
    val updated = for
      body <- parse[TransitionBody](req)
      version <- requireVersion(body.version)
      input <- Validation.transition(body)
      t <- repo.updateTransition(id, input, version)
    yield t
    result(updated)(Response.json(200, _))

  private def result[A](either: Either[WriteError, A])(ok: A => Response): Response =
    either.fold(Response.error, ok)

  /** Writes need a JSON content type. Browsers can't send that cross-site without a CORS preflight, which blocks CSRF. */
  private def write(req: Request)(handler: Request => Response): Response =
    if !writesEnabled then Response.error(403, "Editing is disabled until sign-in is available")
    else if !req.header("content-type").exists(_.toLowerCase.startsWith("application/json")) then
      Response.error(415, "Content-Type must be application/json")
    else handler(req)

  private def parse[A: JsonValueCodec](req: Request): Either[WriteError, A] =
    req.body.filter(_.trim.nonEmpty) match
      case None => Left(WriteError.Invalid("A JSON request body is required"))
      case Some(json) =>
        try Right(readFromString[A](json))
        catch case e: JsonReaderException => Left(WriteError.Invalid(s"Invalid request body: ${e.getMessage.linesIterator.next()}"))

  private def requireVersion(version: Option[Long]): Either[WriteError, Long] =
    version.filter(_ > 0).toRight(WriteError.Invalid("version is required: send the version you last read"))

  private def withId(raw: String)(f: UUID => Response): Response =
    Router.parseId(raw) match
      case Some(id) => f(id)
      case None     => Response.error(400, s"Invalid id '$raw'")

  private def segments(path: String): List[String] =
    path.split('/').iterator.filter(_.nonEmpty).toList

object Router:
  private val CanonicalUuid = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}".r

  /** Parses a canonical 8-4-4-4-12 UUID. UUID.fromString alone also accepts short forms like "1-1-1-1-1". */
  def parseId(raw: String): Option[UUID] =
    Option.when(CanonicalUuid.matches(raw))(UUID.fromString(raw))
