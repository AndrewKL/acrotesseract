package auth

import auth.GoogleAuthConfigUtils.googleAuthSecret
import di.AcroConfig
import org.junit.Test
import org.junit.Assert._

class GoogleAuthConfigUtilsTests {
  @Test
  def googleAuthSecretTests(): Unit ={
    val (clientId, clientSecret) = googleAuthSecret(new AcroConfig().googleAuthSecretName)

    assertNotNull(clientId)
    assertNotNull(clientSecret)
  }
}
