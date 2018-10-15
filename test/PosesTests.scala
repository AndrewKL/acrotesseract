import org.junit.Assert._
import org.junit.Test
import play.Application
import play.api.http.HttpVerbs
import play.test.Helpers
import play.test.Helpers._


class PosesTests{
  val app: Application = fakeApplication()

  @Test
  def renderPosesPage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/poses")
    val result = route(app, request)
    assertEquals(200, result.status)
  }

  @Test
  def renderPosePage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/poses/1")
    val result = route(app, request)
    assertEquals(200, result.status)
  }

  @Test
  def renderPosePage_does_not_exist(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/poses/999999")
    val result = route(app, request)
    assertEquals(404, result.status)
  }


  @Test
  def getNewPose(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/poses/new")
    val result = route(app, request)
    assertEquals(200, result.status)
  }

  @Test
  def getEditPose(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/poses/1/edit")
    val result = route(app, request)
    assertEquals(200, result.status)
  }
}
