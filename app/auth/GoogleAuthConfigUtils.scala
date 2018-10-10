package auth

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.google.inject.Provides
import com.gu.googleauth.{AntiForgeryChecker, GoogleAuthConfig}
import play.api.Configuration
import play.api.http.HttpConfiguration

import scala.util.parsing.json.JSON

object GoogleAuthConfigUtils {
  @Provides
  def googleAuthConfig(configuration:Configuration, httpConfiguration: HttpConfiguration):GoogleAuthConfig = {
    val (clientId, clientSecret) = googleAuthSecret()

    GoogleAuthConfig.withNoDomainRestriction(
      clientId,
      clientSecret,
      redirectUrl = configuration.get[String]("your.redirectUrl.config.path"),
      antiForgeryChecker = AntiForgeryChecker.borrowSettingsFromPlay(httpConfiguration)
    )
  }

  def googleAuthSecret(): (String, String) ={
    val secretName = "acrotesseract-google-auth"
    val region = "us-west-2"
    // Create a Secrets Manager client
    val client = AWSSecretsManagerClientBuilder.standard
      .withCredentials(new ProfileCredentialsProvider("acrotesseract"))
      .withRegion(region).build

    val getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName)
    val getSecretValueResult = client.getSecretValue(getSecretValueRequest)

    //the secret manager secret string is a json object containing the username and password
    val clientId = JSON.parseFull(getSecretValueResult.getSecretString) match {
      case Some(map: Map[String, any]) => map("clientId").toString
    }
    val clientSecret = JSON.parseFull(getSecretValueResult.getSecretString) match {
      case Some(map: Map[String, any]) => map("clientSecret").toString
    }
    return (clientId,clientSecret)
  }
}
