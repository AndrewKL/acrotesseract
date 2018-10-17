package repo

import di.AcroConfig
import org.junit.Assert._
import org.junit.{Ignore, Test}
import repo.AcroDb.getAcroTesseractSecret

class AcroDbIntegrationTests {
  lazy val acrodb = {
    val config = AcroConfig.dev
    val upw = getAcroTesseractSecret(secretName = config.dbPasswordSecretName)
    new AcroDb(jdbc = config.acrotesseractDevJDBC,username = upw.username,pw = upw.password)
  }
  @Ignore
  @Test
  def countPoses(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)

    assertTrue(repo.countPoses() >= 0)
  }

  @Ignore
  @Test
  def countTransistions(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)

    assertTrue(repo.countTransitions() >= 0)
  }

  @Ignore
  @Test
  def insertGetUpdateDeletePose(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)
    val orig = new Pose(
      pose_id = Option.empty,
      name = "ground",
      created_by = "Andrew",
      image_url = Option("image url"),
      description_md = Option("descr")
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
    val repo = new PosesAndTransitionsRepo(acrodb)

    val poses = repo.listPose()

    assertNotNull(poses)
  }

  @Ignore
  @Test
  def insertGetUpdateDeleteTransition(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)

    val pose:Pose = new Pose(
      pose_id = Option.empty,
      name = "ground",
      created_by = "Andrew",
      image_url = Option("image url"),
      description_md = Option("descr")
    )

    val pose_id = repo.insertPose(pose)

    val transition = new Transition(
      Option.empty,
      "name",
      "createdby",
      "desc",
      pose_id,
      pose_id)

    val id = repo.insertTransition(transition)

    println(id)

    val reloaded = repo.getTransition(id).get

    println(transition)
    println(reloaded)

    //assertTrue(reloaded == orig)

    val modified = reloaded.copy(name = "new name")

    repo.updateTransition(modified)

    val reloadedModified = repo.getTransition(id).get
    println(reloadedModified)

    assertEquals(reloadedModified.name,modified.name)

    repo.deleteTransition(id)
    assertTrue(repo.countTransitions() >= 0)
  }

  @Ignore
  @Test
  def listTransitionsFromToPose(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)

    val pose:Pose = new Pose(
      pose_id = Option.empty,
      name = "ground",
      created_by = "Andrew",
      image_url = Option("image url"),
      description_md = Option("descr")
    )

    val pose_id = repo.insertPose(pose)

    val transition = new Transition(
      Option.empty,
      "name",
      "createdby",
      "desc",
      pose_id,
      pose_id)

    val results = repo.listTransitionsFromPose(pose_id)
    assertNotNull(results)

    val toResults = repo.listTransitionsToPose(pose_id)
    assertNotNull(toResults)
  }


  @Ignore
  @Test
  def dumpDb(): Unit = {
    val repo = new PosesAndTransitionsRepo(acrodb)

    println(repo.dumpDb())
  }
}
