package repo

import anorm._
import anorm.SqlParser._
import javax.inject.{Inject, Singleton}

@Singleton
class PosesAndTransistionsRepo @Inject()(db:AcroDb) {
  def countTransitions(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Transitions"""
    sql.as(scalar[Long].singleOpt).get
  }

  def countPoses(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Poses"""
    sql.as(scalar[Long].singleOpt).get
  }

  def getPose(poseId:Long):Option[Pose] = db.withConnection { implicit conn =>
    val parser: RowParser[Pose] = Macro.namedParser[Pose]
    val sql = SQL("""SELECT * FROM Poses WHERE pose_id = {pose_id}""")
    sql.on('pose_id-> poseId).as(parser.*).headOption
  }

  def insertPose(pose:Pose)= db.withConnection { implicit conn =>
    val sql = SQL("""INSERT INTO Poses(name,created_by,image_url,description_md) values ({name},{created_by},{image_url},{description_md})""")
        .on('name-> pose.name,'created_by -> pose.created_by, 'image_url -> pose.image_url, 'description_md -> pose.description_md)
    sql.executeInsert(scalar[Long].single)
  }

  def updatePose(pose:Pose)= db.withConnection { implicit conn =>
    val sql = SQL("""UPDATE Poses set name = {name}, createdBy,image_url,description_md) values ({name},{created_by},{image_url},{description_md})""")
      .on('name-> pose.name,'created_by -> pose.created_by, 'image_url -> pose.image_url, 'description_md -> pose.description_md)
    sql.executeInsert(scalar[Long].single)
  }

  def deletePose(poseId:Long)= db.withConnection { implicit conn =>
    val sql = SQL("""DELETE FROM Poses WHERE pose_id =  {pose_id}""")
      .on('pose_id-> poseId)
    sql.execute()
  }
}

case class Pose(pose_id:Option[Int],name:String,created_by:String,image_url:String,description_md:String)
case class Transition(transition_id:Option[Int])
