package acrotesseract

import java.util.UUID

/** Routes requests to handlers with a plain pattern match, keeping the Lambda startup path short. */
final class Router(stage: String, repo: AcroRepository):
  def handle(req: Request): Response =
    (req.method, segments(req.path)) match
      case ("GET", List("api", "health"))          => Response.json(200, Health("ok", stage))
      case ("GET", List("api", "poses"))           => Response.json(200, repo.listPoses())
      case ("GET", List("api", "poses", id))       => withId(id)(getPose)
      case ("GET", List("api", "transitions"))     => Response.json(200, repo.listTransitions())
      case ("GET", List("api", "transitions", id)) => withId(id)(getTransition)
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

  /** Builds the router from environment variables set by CDK (or by sbt for local runs). */
  def fromEnv(): Router =
    val stage = sys.env.getOrElse("STAGE", "local")
    Router(stage, StaticData.load())
