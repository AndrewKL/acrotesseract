package db

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import org.junit.{Ignore, Test}

import scala.util.parsing.json.JSON

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
class InitializeDB {

  def getAcroTesseractSecret(): UsernamePW = {
    val secretName = "acrotesseract-rds-username-pw"
    val region = "us-west-2"
    // Create a Secrets Manager client
    val client = AWSSecretsManagerClientBuilder.standard
      .withCredentials(new ProfileCredentialsProvider("acrotesseract"))
      .withRegion(region).build

    val getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName)
    val getSecretValueResult = client.getSecretValue(getSecretValueRequest)

    //the secret manager secret string is a json object containing the username and password
    val pw = JSON.parseFull(getSecretValueResult.getSecretString) match {
      case Some(map: Map[String, any]) => map("password").toString
    }
    val username = JSON.parseFull(getSecretValueResult.getSecretString) match {
      case Some(map: Map[String, any]) => map("username").toString
    }
    return UsernamePW(username, pw)

  }

  val acrotesseractDevJDBC = "jdbc:mysql://acrotesseract-db.cgccqt70jhl5.us-west-2.rds.amazonaws.com:3306/dev"

  @Test
  @Ignore
  def migrateDB(): Unit = {
    val usernamePw = getAcroTesseractSecret()

    import org.flywaydb.core.Flyway
    // Create the Flyway instance and point it to the database// Create the Flyway instance and point it to the database

    val flyway: Flyway = Flyway.configure
      .dataSource(acrotesseractDevJDBC, usernamePw.username, usernamePw.password)
      .load

    // Start the migration

    flyway.repair()

    flyway.migrate()

  }

}

case class UsernamePW(username: String, password: String)
