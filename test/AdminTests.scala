import org.junit.Assert._
import org.junit.Test
import play.Application
import play.api.http.HttpVerbs
import play.test.Helpers
import play.test.Helpers._


class AdminTests{
  val app: Application = fakeApplication()

  @Test
  def renderAdminPage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/admin")
    val result = route(app, request)
    assertEquals(200, result.status)
  }
}
