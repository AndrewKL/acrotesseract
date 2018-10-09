import org.junit.Assert._
import org.junit.Test
import play.Application
import play.api.http.HttpVerbs
import play.test.Helpers
import play.test.Helpers._


class TransitionsTests{
  val app: Application = fakeApplication()

  @Test
  def renderTransitionsPage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/transitions")
    val result = route(app, request)
    assertEquals(200, result.status)
  }

  @Test
  def renderPosePage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/transitions/1")
    val result = route(app, request)
    assertEquals(200, result.status)
  }

  @Test
  def renderPosePage_does_not_exist(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/transitions/999999")
    val result = route(app, request)
    assertEquals(404, result.status)
  }
}
