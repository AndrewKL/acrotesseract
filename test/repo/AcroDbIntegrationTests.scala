package repo

import org.junit.Assert._
import org.junit.Test

class AcroDbIntegrationTests {
  @Test
  def countPoses(): Unit = {
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransistionsRepo(acrodb)

    assertTrue(repo.countPoses() >= 0)
  }

  @Test
  def countTransistions(): Unit ={
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransistionsRepo(acrodb)

    assertTrue(repo.countTransitions() >= 0)
  }

  @Test
  def insertGetDeletePose(): Unit = {
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransistionsRepo(acrodb)
    val orig = new Pose(
      pose_id = Option.empty,
      name = "ground",
      created_by = "Andrew",
      image_url = null,
      description_md = null
    )

    val id = repo.insertPose(orig)

    println(id)

    val reloaded = repo.getPose(id)

    assertTrue(reloaded == orig)

    repo.deletePose(id)
    assertTrue(repo.countTransitions() >= 0)
  }
}
