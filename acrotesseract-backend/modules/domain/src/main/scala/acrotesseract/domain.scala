package acrotesseract

import java.util.UUID

/** A pose is a node in the acro graph. `version` starts at 1 and goes up by one on every update. */
final case class Pose(
    id: UUID,
    name: String,
    imageUrl: Option[String],
    descriptionMd: Option[String],
    version: Long
)

/** A transition is a directed edge from one pose to another. Parallel edges and self-loops are allowed. */
final case class Transition(
    id: UUID,
    name: String,
    descriptionMd: Option[String],
    poseFrom: UUID,
    poseTo: UUID,
    youtubeUrl: Option[String],
    version: Long
)

/** The editable fields of a pose, already validated and normalized (see Validation). */
final case class PoseInput(name: String, imageUrl: Option[String], descriptionMd: Option[String])

/** The editable fields of a transition, already validated and normalized. */
final case class TransitionInput(
    name: String,
    descriptionMd: Option[String],
    poseFrom: UUID,
    poseTo: UUID,
    youtubeUrl: Option[String]
)

/** Why a write was rejected. The router maps these to 400, 404 and 409. */
enum WriteError:
  case Invalid(message: String)
  case NotFound(message: String)
  case Conflict(message: String)
