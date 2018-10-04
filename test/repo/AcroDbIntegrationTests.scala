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
}
