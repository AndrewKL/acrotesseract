import org.junit.Assert._
import org.junit.{Ignore, Test}
import play.Application
import play.api.http.HttpVerbs
import play.test.Helpers
import play.test.Helpers._


class AdminTests{
  val app: Application = fakeApplication()
  @Ignore // This is disabled until there's a way to test methods with security enabled
  @Test
  def renderAdminPage(): Unit = {
    val request = Helpers.fakeRequest.method(HttpVerbs.GET).uri("/admin")
    val result = route(app, request)
    assertEquals(200, result.status)
  }
}
