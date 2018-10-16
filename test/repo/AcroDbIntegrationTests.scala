package repo

import org.junit.Assert._
import org.junit.{Ignore, Test}

class AcroDbIntegrationTests {
  @Ignore
  @Test
  def countPoses(): Unit = {
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransitionsRepo(acrodb)

    assertTrue(repo.countPoses() >= 0)
  }

  @Ignore
  @Test
  def countTransistions(): Unit ={
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransitionsRepo(acrodb)

    assertTrue(repo.countTransitions() >= 0)
  }

  @Ignore
  @Test
  def insertGetUpdateDeletePose(): Unit = {
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransitionsRepo(acrodb)
    val orig = new Pose(
      pose_id = Option.empty,
      name = "ground",
      created_by = "Andrew",
      image_url = Option("image url"),
      description_md = "descr"
    )

    val id = repo.insertPose(orig)

    println(id)

    val reloaded = repo.getPose(id).get

    println(orig)
    println(reloaded)

    //assertTrue(reloaded == orig)

    val modified = reloaded.copy(name = "new name")

    repo.updatePose(modified)

    val reloadedModified = repo.getPose(id).get
    println(reloadedModified)

    assertEquals(reloadedModified.name,modified.name)

    repo.deletePose(id)
    assertTrue(repo.countTransitions() >= 0)
  }

  @Ignore
  @Test
  def listPoses(): Unit = {
    val acrodb = AcroDb.devDb
    val repo = new PosesAndTransitionsRepo(acrodb)

    val poses = repo.listPose()

    assertNotNull(poses)
  }
}
