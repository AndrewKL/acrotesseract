import org.junit.Assert._
import org.junit.{Ignore, Test}
import play.api.test._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec  {
  val port = 9000
  @Test
  def canStartLocalDevServer(): Unit = new WithBrowser {
    browser.goTo(s"http://localhost:$port")
    assertNotNull(browser.pageSource)
  }
}
