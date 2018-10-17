package repo

object TestData {
  val poses = List(
    new Pose(
      pose_id = Option(1),
      name = "Ground",
      created_by = "Andrew",
      image_url = null,
      description_md = Option("null")
    ),
    new Pose(
      pose_id = Option(2),
      name = "Bird",
      created_by = "Andrew",
      image_url = Option("/assets/acrotesseract/img/poses/bird.jpeg"),
      description_md = Option("null")
    ),
    new Pose(
      pose_id = Option(3),
      name = "Star",
      created_by = "Andrew",
      image_url = Option("/assets/acrotesseract/img/poses/star_side_view.jpg"),
      description_md = Option("null")
    ),
    new Pose(
      pose_id = Option(4),
      name = "Shoulder Stand",
      created_by = "Andrew",
      image_url = Option("/assets/acrotesseract/img/poses/free_shoulder_stand.jpg"),
      description_md = Option("null")
    )
  )

  val transitions = List(
    new Transition(
      transition_id = Option(1),
      name = "ground to bird",
      created_by = "Andrew",
      description_md = null,
      pose_from = 1,
      pose_to = 2
    ),
    new Transition(
      transition_id = Option(2),
      name = "shoulder stand",
      created_by = "Andrew",
      description_md = null,
      pose_from = 2,
      pose_to = 4
    ),
    new Transition(
      transition_id = Option(3),
      name = "Corkscrew up",
      created_by = "Andrew",
      description_md = null,
      pose_from = 2,
      pose_to = 3
    ),
    new Transition(
      transition_id = Option(3),
      name = "Corkscrew down",
      created_by = "Andrew",
      description_md = null,
      pose_from = 3,
      pose_to = 2
    )
  )
}

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
}
