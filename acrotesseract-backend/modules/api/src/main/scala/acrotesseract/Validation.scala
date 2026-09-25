package acrotesseract

import java.net.URI
import java.util.UUID
import scala.util.Try

/** Request bodies for POST and PUT. `version` is required on PUT (optimistic locking) and ignored on POST. */
final case class PoseBody(
    name: String,
    imageUrl: Option[String],
    descriptionMd: Option[String],
    version: Option[Long]
)

final case class TransitionBody(
    name: String,
    descriptionMd: Option[String],
    poseFrom: UUID,
    poseTo: UUID,
    youtubeUrl: Option[String],
    version: Option[Long]
)

/** Trims and checks request bodies. Limits match the legacy RDS columns (see data/data.schema.json). */
object Validation:
  val MaxName = 256
  val MaxImageUrl = 256
  val MaxDescription = 10000
  val MaxYoutubeUrl = 1024

  def pose(body: PoseBody): Either[WriteError, PoseInput] =
    for
      name <- requiredText("name", body.name, MaxName)
      imageUrl <- optionalUrl("imageUrl", body.imageUrl, MaxImageUrl)
      description <- optionalText("descriptionMd", body.descriptionMd, MaxDescription)
    yield PoseInput(name, imageUrl, description)

  def transition(body: TransitionBody): Either[WriteError, TransitionInput] =
    for
      name <- requiredText("name", body.name, MaxName)
      description <- optionalText("descriptionMd", body.descriptionMd, MaxDescription)
      youtubeUrl <- optionalUrl("youtubeUrl", body.youtubeUrl, MaxYoutubeUrl)
      _ <- youtubeUrl.filterNot(isYoutube).map(_ => invalid("youtubeUrl must be a YouTube link")).toLeft(())
    yield TransitionInput(name, description, body.poseFrom, body.poseTo, youtubeUrl)

  private def invalid(message: String) = WriteError.Invalid(message)

  private def requiredText(field: String, value: String, max: Int): Either[WriteError, String] =
    val trimmed = value.trim
    if trimmed.isEmpty then Left(invalid(s"$field is required"))
    else if trimmed.length > max then Left(invalid(s"$field must be at most $max characters"))
    else Right(trimmed)

  /** Blank strings count as absent. */
  private def optionalText(field: String, value: Option[String], max: Int): Either[WriteError, Option[String]] =
    value.map(_.trim).filter(_.nonEmpty) match
      case Some(v) if v.length > max => Left(invalid(s"$field must be at most $max characters"))
      case other                     => Right(other)

  private def optionalUrl(field: String, value: Option[String], max: Int): Either[WriteError, Option[String]] =
    optionalText(field, value, max).flatMap {
      case Some(url) if !isHttpUrl(url) => Left(invalid(s"$field must be an http(s) URL"))
      case other                        => Right(other)
    }

  private def host(url: String): Option[String] = Try(URI(url)).toOption.flatMap(u => Option(u.getHost)).map(_.toLowerCase)

  private def isHttpUrl(url: String): Boolean =
    Try(URI(url)).toOption.exists(u => Set("http", "https").contains(u.getScheme) && u.getHost != null)

  private def isYoutube(url: String): Boolean =
    host(url).exists(h => h == "youtu.be" || h == "youtube.com" || h.endsWith(".youtube.com"))
