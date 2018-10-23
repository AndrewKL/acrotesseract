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
      pose_to = 2,
      Option("https://youtu.be/g8OhDBRwhSw?t=403")
    ),
    new Transition(
      transition_id = Option(2),
      name = "shoulder stand",
      created_by = "Andrew",
      description_md = null,
      pose_from = 2,
      pose_to = 4,
      Option("https://youtu.be/g8OhDBRwhSw?t=403")
    ),
    new Transition(
      transition_id = Option(3),
      name = "Corkscrew up",
      created_by = "Andrew",
      description_md = null,
      pose_from = 2,
      pose_to = 3,
      Option("https://youtu.be/g8OhDBRwhSw?t=403")
    ),
    new Transition(
      transition_id = Option(3),
      name = "Corkscrew down",
      created_by = "Andrew",
      description_md = null,
      pose_from = 3,
      pose_to = 2,
      Option("https://youtu.be/g8OhDBRwhSw?t=403")
    )
  )
}


