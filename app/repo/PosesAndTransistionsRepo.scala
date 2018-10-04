package repo

import anorm._
import javax.inject.{Inject, Singleton}

@Singleton
class PosesAndTransistionsRepo @Inject()(db:AcroDb) {
  def countTransitions(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Transitions"""
    sql.as(anorm.SqlParser.scalar[Long].singleOpt).get
  }

  def countPoses(): Long = db.withConnection { implicit conn =>
    val sql = SQL"""SELECT count(*) FROM Poses"""
    sql.as(anorm.SqlParser.scalar[Long].singleOpt).get
  }
}
