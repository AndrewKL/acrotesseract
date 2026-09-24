package acrotesseract

import java.util.UUID

/** Read access to poses and transitions. A DynamoDB implementation will replace the static one. */
trait AcroRepository:
  def listPoses(): List[Pose]
  def getPose(id: UUID): Option[Pose]
  def listTransitions(): List[Transition]
  def getTransition(id: UUID): Option[Transition]
  def transitionsFrom(poseId: UUID): List[Transition]
  def transitionsTo(poseId: UUID): List[Transition]

/** An in-memory repository over a fixed data set. Lists are sorted by name, case-insensitively. */
final class InMemoryRepository(poses: List[Pose], transitions: List[Transition]) extends AcroRepository:
  private val posesById = poses.map(p => p.id -> p).toMap
  private val transitionsById = transitions.map(t => t.id -> t).toMap
  private val sortedPoses = poses.sortBy(_.name.toLowerCase)
  private val sortedTransitions = transitions.sortBy(_.name.toLowerCase)
  private val byFrom = sortedTransitions.groupBy(_.poseFrom).withDefaultValue(Nil)
  private val byTo = sortedTransitions.groupBy(_.poseTo).withDefaultValue(Nil)

  def listPoses(): List[Pose] = sortedPoses
  def getPose(id: UUID): Option[Pose] = posesById.get(id)
  def listTransitions(): List[Transition] = sortedTransitions
  def getTransition(id: UUID): Option[Transition] = transitionsById.get(id)
  def transitionsFrom(poseId: UUID): List[Transition] = byFrom(poseId)
  def transitionsTo(poseId: UUID): List[Transition] = byTo(poseId)
