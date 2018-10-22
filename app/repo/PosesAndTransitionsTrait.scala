package repo

trait PosesAndTransitionsTrait {

  def countTransitions(): Long

  def countPoses(): Long

  def getPose(poseId: Long): Option[Pose]

  def listPose(): List[Pose]

  def searchPose(searchText:String): List[Pose]

  def insertPose(pose: Pose): Long

  def updatePose(pose: Pose)

  def deletePose(poseId: Long)

  def getTransition(transitionId: Long): Option[Transition]

  def listTransitions():List[Transition]

  def searchTransitions(searchText:String): List[Transition]

  def listTransitionsFromPose(poseId:Long): List[Transition]

  def listTransitionsToPose(poseId:Long): List[Transition]

  def insertTransition(transition: Transition): Long

  def updateTransition(transition: Transition)

  def deleteTransition(transitionId: Long)
}
