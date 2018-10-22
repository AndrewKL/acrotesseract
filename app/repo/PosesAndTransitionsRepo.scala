package repo

import anorm._
import anorm.SqlParser._
import javax.inject.{Inject, Singleton}

@Singleton
class PosesAndTransitionsRepo @Inject()(db:AcroDb) extends PosesAndTransitionsTrait {
  override def countTransitions(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Transitions"""
    sql.as(scalar[Long].singleOpt).get
  }

  override def countPoses(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Poses"""
    sql.as(scalar[Long].singleOpt).get
  }

  override def getPose(poseId:Long):Option[Pose] = db.withConnection { implicit conn =>
    val parser: RowParser[Pose] = Macro.namedParser[Pose]
    val sql = SQL("""SELECT * FROM Poses WHERE pose_id = {pose_id}""")
    sql.on('pose_id-> poseId).as(parser.*).headOption
  }

  override def insertPose(pose:Pose): Long = db.withConnection { implicit conn =>
    val sql = SQL(
      """INSERT INTO Poses(name,created_by,image_url,description_md)
        |VALUES ({name},{created_by},{image_url},{description_md})""".stripMargin)
      .on(
        "name" -> pose.name,
        "created_by" -> pose.created_by,
        "image_url" -> pose.image_url.get,
        "description_md" -> pose.description_md)

    sql.executeInsert(scalar[Long].single)
  }

  override def updatePose(pose:Pose)= db.withConnection { implicit conn =>
    val sql = SQL(
      """UPDATE Poses set
        | name = {name},
        | created_by = {created_by},
        | image_url = {image_url},
        | description_md = {description_md}
        |WHERE pose_id = {pose_id}""".stripMargin)
      .on(
        "name" -> pose.name,
        "created_by" -> pose.created_by,
        "image_url" -> pose.image_url,
        "description_md" -> pose.description_md,
        "pose_id" -> pose.pose_id.get)
    sql.executeUpdate()
  }

  override def deletePose(poseId:Long)= db.withConnection { implicit conn =>
    val sql = SQL("""DELETE FROM Poses WHERE pose_id =  {pose_id}""")
      .on('pose_id-> poseId)
    sql.execute()
  }

  override def listPose(): List[Pose] = db.withConnection { implicit conn =>
    val parser: RowParser[Pose] = Macro.namedParser[Pose]
    val sql = SQL("""SELECT * FROM Poses""")
    sql.as(parser.*)
  }

  override def searchPose(searchText:String): List[Pose] = db.withConnection { implicit conn =>
    val parser: RowParser[Pose] = Macro.namedParser[Pose]
    val sql = SQL("""SELECT * FROM Poses WHERE name LIKE {searchText}  LIMIT 10""")
      .on("searchText" -> s"${searchText.toLowerCase}%")
    sql.as(parser.*)
  }

  override def getTransition(transitionId: Long): Option[Transition] = db.withConnection { implicit conn =>
    val parser: RowParser[Transition] = Macro.namedParser[Transition]
    val sql = SQL("""SELECT * FROM Transitions WHERE transition_id = {transition_id}""")
    sql.on('transition_id-> transitionId).as(parser.*).headOption
  }

  override def insertTransition(transition: Transition): Long = db.withConnection { implicit conn =>
    val sql = SQL(
      """INSERT INTO Transitions(name,created_by,description_md,pose_from,pose_to)
        |VALUES ({name},{created_by},{description_md},{pose_from},{pose_to})""".stripMargin)
      .on(
        "name" -> transition.name,
        "created_by" -> transition.created_by,
        "description_md" -> transition.description_md,
        "pose_from" -> transition.pose_from,
        "pose_to" -> transition.pose_to)

    sql.executeInsert(scalar[Long].single)
  }

  override def updateTransition(transition: Transition): Unit = db.withConnection { implicit conn =>
    val sql = SQL(
      """UPDATE Transitions set
        | name = {name},
        | created_by = {created_by},
        | description_md = {description_md},
        | pose_to = {pose_to},
        | pose_from = {pose_from}
        |WHERE transition_id = {transition_id}""".stripMargin)
      .on(
        "name" -> transition.name,
        "created_by" -> transition.created_by,
        "description_md" -> transition.description_md,
        "pose_from" -> transition.pose_from,
        "pose_to" -> transition.pose_to,
        "transition_id" -> transition.transition_id.get
      )
    sql.executeUpdate()
  }

  override def deleteTransition(transitionId: Long): Unit = db.withConnection { implicit conn =>
    val sql = SQL("""DELETE FROM Transitions WHERE transition_id =  {transition_id}""")
      .on('transition_id-> transitionId)
    sql.execute()
  }

  override def listTransitions(): List[Transition] = db.withConnection { implicit conn =>
    val parser: RowParser[Transition] = Macro.namedParser[Transition]
    val sql = SQL("""SELECT * FROM Transitions""")
    sql.as(parser.*)
  }

  def searchTransitions(searchText:String): List[Transition]= db.withConnection { implicit conn =>
    val parser: RowParser[Transition] = Macro.namedParser[Transition]
    val sql = SQL("""SELECT * FROM Transitions WHERE name LIKE {searchText} LIMIT 10""")
      .on("searchText" -> s"${searchText.toLowerCase}%")
    sql.as(parser.*)
  }

  override def listTransitionsFromPose(poseId: Long): List[Transition] = db.withConnection { implicit conn =>
    val parser: RowParser[Transition] = Macro.namedParser[Transition]
    val sql = SQL("""SELECT * FROM Transitions WHERE pose_from = {poseId}""")
      .on("poseId"->poseId)
    sql.as(parser.*)
  }

  override def listTransitionsToPose(poseId: Long): List[Transition] = db.withConnection { implicit conn =>
    val parser: RowParser[Transition] = Macro.namedParser[Transition]
    val sql = SQL("""SELECT * FROM Transitions WHERE pose_to = {poseId}""")
      .on("poseId"->poseId)
    sql.as(parser.*)
  }

  def dumpDb():PosesTransitions = new PosesTransitions(
    poses = listPose(),
    transition = listTransitions()
  )
}

case class PosesTransitions(poses:List[Pose],transition:List[Transition])



