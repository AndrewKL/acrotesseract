package repo

class TestRepo extends PosesAndTransitionsTrait {
  override def countTransitions(): Long = TestData.transitions.size

  override def countPoses(): Long = TestData.poses.size

  override def getPose(poseId: Long): Option[Pose] = TestData.poses.find(x=>x.pose_id.getOrElse(-1) == poseId)

  override def insertPose(pose: Pose): Long = 1 //DO NOTHING FOR TESTING

  override def updatePose(pose: Pose): Unit = {/* DO NOTHING */}

  override def deletePose(poseId: Long): Unit = ???

  override def getTransition(transitionId: Long): Option[Transition] = TestData.transitions.find(x=>x.transition_id.getOrElse(-1) == transitionId)

  override def insertTransition(transition: Transition): Long = ???

  override def updateTransition(transition: Transition): Unit = {} // DO NOTHING

  override def deleteTransition(transitionId: Long): Unit = ???

  override def listPose(): List[Pose] = TestData.poses

  override def listTransitions(): List[Transition] = TestData.transitions

  override def listTransitionsFromPose(poseId: Long): List[Transition] = TestData.transitions.filter(x=>x.pose_from == poseId)

  override def listTransitionsToPose(poseId: Long): List[Transition] = TestData.transitions.filter(x=>x.pose_to == poseId)

  override def searchPose(searchText: String): List[Pose] = ???

  override def searchTransitions(searchText: String): List[Transition] = ???
}
