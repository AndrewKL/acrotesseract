package auth

import auth.GoogleAuthConfigUtils.googleAuthSecret
import org.junit.Test
import org.junit.Assert._

class GoogleAuthConfigUtilsTests {
  @Test
  def googleAuthSecretTests(): Unit ={
    val (clientId, clientSecret) = googleAuthSecret()

    assertNotNull(clientId)
    assertNotNull(clientSecret)
  }
}
