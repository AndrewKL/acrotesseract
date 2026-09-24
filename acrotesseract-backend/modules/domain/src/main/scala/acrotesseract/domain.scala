package acrotesseract

import java.util.UUID

/** A pose is a node in the acro graph. */
final case class Pose(
    id: UUID,
    name: String,
    imageUrl: Option[String],
    descriptionMd: Option[String]
)

/** A transition is a directed edge from one pose to another. Parallel edges and self-loops are allowed. */
final case class Transition(
    id: UUID,
    name: String,
    descriptionMd: Option[String],
    poseFrom: UUID,
    poseTo: UUID,
    youtubeUrl: Option[String]
)
