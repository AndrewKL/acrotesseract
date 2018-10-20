package auth

import auth.GoogleAuthConfigUtils.googleAuthSecret
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import di.AcroConfig
import org.junit.Test
import org.junit.Assert._

class GoogleAuthConfigUtilsTests {
  @Test
  def googleAuthSecretTests(): Unit = {
    val (clientId, clientSecret) = googleAuthSecret(AcroConfig.dev.googleAuthSecretName,new DefaultAWSCredentialsProviderChain)

    assertNotNull(clientId)
    assertNotNull(clientSecret)
  }
}
