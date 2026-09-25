package acrotesseract

import java.util.UUID

/** Storage for poses and transitions. Implemented by InMemoryRepository (local dev, tests) and DynamoDbRepository. */
trait AcroRepository:
  def listPoses(): List[Pose]
  def getPose(id: UUID): Option[Pose]
  def listTransitions(): List[Transition]
  def getTransition(id: UUID): Option[Transition]
  def transitionsFrom(poseId: UUID): List[Transition]
  def transitionsTo(poseId: UUID): List[Transition]

  /** Creates a pose with a new id and version 1. Names are unique, ignoring case. */
  def createPose(input: PoseInput): Either[WriteError, Pose]

  /** Replaces a pose's fields if its current version is `expectedVersion`, and bumps the version. */
  def updatePose(id: UUID, input: PoseInput, expectedVersion: Long): Either[WriteError, Pose]

  /** Creates a transition with a new id and version 1. Both poses must exist; names are unique, ignoring case. */
  def createTransition(input: TransitionInput): Either[WriteError, Transition]

  /** Replaces a transition's fields if its current version is `expectedVersion`, and bumps the version. */
  def updateTransition(id: UUID, input: TransitionInput, expectedVersion: Long): Either[WriteError, Transition]

/** Messages shared by every repository, so both implementations answer the same way. */
object RepositoryErrors:
  def poseNameTaken(name: String) = WriteError.Conflict(s"A pose named '$name' already exists")
  def transitionNameTaken(name: String) = WriteError.Conflict(s"A transition named '$name' already exists")
  def poseNotFound(id: UUID) = WriteError.NotFound(s"Pose $id not found")
  def transitionNotFound(id: UUID) = WriteError.NotFound(s"Transition $id not found")
  def missingPose(field: String, id: UUID) = WriteError.Invalid(s"$field pose $id does not exist")
  def staleVersion(what: String, expected: Long, current: Long) =
    WriteError.Conflict(s"$what was changed by someone else (version $current, expected $expected). Reload and try again.")

/** A thread-safe in-memory repository. Lists are sorted by name, case-insensitively. */
final class InMemoryRepository(initialPoses: List[Pose], initialTransitions: List[Transition]) extends AcroRepository:
  import RepositoryErrors.*

  private var poses: Map[UUID, Pose] = initialPoses.map(p => p.id -> p).toMap
  private var transitions: Map[UUID, Transition] = initialTransitions.map(t => t.id -> t).toMap

  private def byName[A](items: Iterable[A])(name: A => String): List[A] = items.toList.sortBy(a => name(a).toLowerCase)

  def listPoses(): List[Pose] = synchronized(byName(poses.values)(_.name))
  def getPose(id: UUID): Option[Pose] = synchronized(poses.get(id))
  def listTransitions(): List[Transition] = synchronized(byName(transitions.values)(_.name))
  def getTransition(id: UUID): Option[Transition] = synchronized(transitions.get(id))
  def transitionsFrom(poseId: UUID): List[Transition] = listTransitions().filter(_.poseFrom == poseId)
  def transitionsTo(poseId: UUID): List[Transition] = listTransitions().filter(_.poseTo == poseId)

  private def poseNameUsed(name: String, except: Option[UUID]) =
    poses.values.exists(p => p.name.equalsIgnoreCase(name) && !except.contains(p.id))
  private def transitionNameUsed(name: String, except: Option[UUID]) =
    transitions.values.exists(t => t.name.equalsIgnoreCase(name) && !except.contains(t.id))

  def createPose(input: PoseInput): Either[WriteError, Pose] = synchronized {
    if poseNameUsed(input.name, None) then Left(poseNameTaken(input.name))
    else
      val pose = Pose(UUID.randomUUID(), input.name, input.imageUrl, input.descriptionMd, 1)
      poses += pose.id -> pose
      Right(pose)
  }

  def updatePose(id: UUID, input: PoseInput, expectedVersion: Long): Either[WriteError, Pose] = synchronized {
    poses.get(id) match
      case None => Left(poseNotFound(id))
      case Some(current) if current.version != expectedVersion =>
        Left(staleVersion("This pose", expectedVersion, current.version))
      case Some(_) if poseNameUsed(input.name, Some(id)) => Left(poseNameTaken(input.name))
      case Some(current) =>
        val pose = Pose(id, input.name, input.imageUrl, input.descriptionMd, current.version + 1)
        poses += id -> pose
        Right(pose)
  }

  private def checkPoses(input: TransitionInput): Option[WriteError] =
    if !poses.contains(input.poseFrom) then Some(missingPose("From", input.poseFrom))
    else if !poses.contains(input.poseTo) then Some(missingPose("To", input.poseTo))
    else None

  def createTransition(input: TransitionInput): Either[WriteError, Transition] = synchronized {
    checkPoses(input) match
      case Some(error)                                   => Left(error)
      case None if transitionNameUsed(input.name, None) => Left(transitionNameTaken(input.name))
      case None =>
        val t = Transition(UUID.randomUUID(), input.name, input.descriptionMd, input.poseFrom, input.poseTo, input.youtubeUrl, 1)
        transitions += t.id -> t
        Right(t)
  }

  def updateTransition(id: UUID, input: TransitionInput, expectedVersion: Long): Either[WriteError, Transition] =
    synchronized {
      transitions.get(id) match
        case None => Left(transitionNotFound(id))
        case Some(current) if current.version != expectedVersion =>
          Left(staleVersion("This transition", expectedVersion, current.version))
        case Some(current) =>
          checkPoses(input) match
            case Some(error)                                        => Left(error)
            case None if transitionNameUsed(input.name, Some(id)) => Left(transitionNameTaken(input.name))
            case None =>
              val t = Transition(id, input.name, input.descriptionMd, input.poseFrom, input.poseTo, input.youtubeUrl, current.version + 1)
              transitions += id -> t
              Right(t)
    }
