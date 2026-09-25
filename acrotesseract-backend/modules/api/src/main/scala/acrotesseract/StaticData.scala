package acrotesseract

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import java.util.UUID

/** Loads data/data.json, which sbt bundles onto the classpath as `data.json`.
  *
  * The file uses the legacy RDS column names (pose_id, pose_from, ...); see data/data.schema.json.
  */
object StaticData:
  val resourceName = "data.json"

  private final case class PoseRow(
      poseId: UUID,
      name: String,
      imageUrl: Option[String],
      descriptionMd: Option[String]
  )

  private final case class TransitionRow(
      transitionId: UUID,
      name: String,
      descriptionMd: Option[String],
      poseFrom: UUID,
      poseTo: UUID,
      youtubeUrl: Option[String]
  )

  private final case class DataFile(poses: List[PoseRow], transitions: List[TransitionRow])

  // Unknown fields ($schema, created_ts, created_by) are skipped.
  private given JsonValueCodec[DataFile] =
    JsonCodecMaker.make(CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case))

  /** An in-memory repository over the bundled data. Throws if the resource is missing or the data is inconsistent. */
  def load(): InMemoryRepository =
    val (poses, transitions) = loadData()
    InMemoryRepository(poses, transitions)

  def parse(bytes: Array[Byte]): InMemoryRepository =
    val (poses, transitions) = parseData(bytes)
    InMemoryRepository(poses, transitions)

  /** The bundled data, parsed and checked. Also used to seed DynamoDB. */
  def loadData(): (List[Pose], List[Transition]) =
    val stream = Option(getClass.getClassLoader.getResourceAsStream(resourceName))
      .getOrElse(throw IllegalStateException(s"$resourceName is not on the classpath"))
    try parseData(stream.readAllBytes())
    finally stream.close()

  def parseData(bytes: Array[Byte]): (List[Pose], List[Transition]) =
    val file = readFromArray[DataFile](bytes)
    val poses = file.poses.map(r => Pose(r.poseId, r.name, r.imageUrl, r.descriptionMd, version = 1))
    val transitions = file.transitions.map(r =>
      Transition(r.transitionId, r.name, r.descriptionMd, r.poseFrom, r.poseTo, r.youtubeUrl, version = 1)
    )
    validate(poses, transitions)
    (poses, transitions)

  /** The rules JSON Schema can't express: unique ids and names, and transitions pointing at real poses. */
  private def validate(poses: List[Pose], transitions: List[Transition]): Unit =
    def duplicates[A](values: List[A]): List[A] =
      values.groupBy(identity).collect { case (v, vs) if vs.size > 1 => v }.toList

    val poseIds = poses.map(_.id).toSet
    val problems =
      duplicates(poses.map(_.id)).map(id => s"duplicate pose_id $id") ++
        duplicates(poses.map(_.name)).map(n => s"duplicate pose name '$n'") ++
        duplicates(transitions.map(_.id)).map(id => s"duplicate transition_id $id") ++
        duplicates(transitions.map(_.name)).map(n => s"duplicate transition name '$n'") ++
        transitions.flatMap(t =>
          List(t.poseFrom, t.poseTo).filterNot(poseIds).map(p => s"transition ${t.id} refers to missing pose $p")
        )
    if problems.nonEmpty then
      throw IllegalArgumentException(s"Invalid $resourceName: ${problems.mkString("; ")}")
