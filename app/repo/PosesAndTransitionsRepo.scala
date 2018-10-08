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
    val sql = SQL("""INSERT INTO Poses(name,created_by,image_url,description_md) values ({name},{created_by},{image_url},{description_md})""")
      .on('name-> pose.name,'created_by -> pose.created_by, 'image_url -> pose.image_url, 'description_md -> pose.description_md)
    sql.executeInsert(scalar[Long].single)
  }

  override def updatePose(pose:Pose)= db.withConnection { implicit conn =>
    val sql = SQL("""UPDATE Poses set name = {name}, createdBy,image_url,description_md) values ({name},{created_by},{image_url},{description_md})""")
      .on('name-> pose.name,'created_by -> pose.created_by, 'image_url -> pose.image_url, 'description_md -> pose.description_md)
    sql.executeInsert(scalar[Long].single)
  }

  override def deletePose(poseId:Long)= db.withConnection { implicit conn =>
    val sql = SQL("""DELETE FROM Poses WHERE pose_id =  {pose_id}""")
      .on('pose_id-> poseId)
    sql.execute()
  }

  override def listPose(): List[Pose] = ???

  override def getTransition(transitionId: Long): Option[Transition] = ???

  override def insertTransition(transition: Transition): Long = ???

  override def updateTransition(transition: Transition): Unit = ???

  override def deleteTransition(transitionId: Long): Unit = ???

  override def listTransitions(): List[Transition] = ???

  override def listTransitionsFromPose(poseId: Long): List[Transition] = ???

  override def listTransitionsToPose(poseId: Long): List[Transition] = ???
}

case class Pose(pose_id:Option[Int],name:String,created_by:String,image_url:String,description_md:String)
case class Transition(transition_id:Option[Int],name:String,created_by:String,image_url:String,description_md:String,
                      pose_from:Int, pose_to:Int)