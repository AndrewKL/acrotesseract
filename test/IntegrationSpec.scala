import org.junit.Assert._
import org.junit.Test
import play.api.test._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec  {
  @Test
  def canStartLocalDevServer(): Unit = new WithBrowser {

    browser.goTo("http://localhost:" + port)

    assertNotNull(browser.pageSource)
  }
}
