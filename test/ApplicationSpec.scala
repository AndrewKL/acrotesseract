import org.junit.Assert._
import org.junit.Test
import play.Application
import play.api.http.HttpVerbs
import play.test.Helpers
import play.test.Helpers._


class ApplicationTests{

  val app: Application = fakeApplication()

  //val app: Application = fakeApplication(inMemoryDatabase("test"))

  @Test
  def testBadRoute(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/xx/Kiwi")
    val result = route(app, request)
    assertEquals(404, result.status)
  }

  @Test
  def renderHomePage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/")
    val result = route(app, request)
    assertEquals(200, result.status)
  }
}
