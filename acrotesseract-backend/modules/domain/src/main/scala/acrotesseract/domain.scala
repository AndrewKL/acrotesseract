package acrotesseract

/** A pose is a node in the acro graph. */
final case class Pose(
    id: Long,
    name: String,
    imageUrl: Option[String],
    descriptionMd: Option[String]
)

/** A transition is a directed edge from one pose to another. Parallel edges and self-loops are allowed. */
final case class Transition(
    id: Long,
    name: String,
    descriptionMd: Option[String],
    poseFrom: Long,
    poseTo: Long,
    youtubeUrl: Option[String]
)
