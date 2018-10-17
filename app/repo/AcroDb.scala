package repo

import java.sql.Connection

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}
import javax.inject.Singleton

import scala.util.parsing.json.JSON

object AcroDb {

  def getAcroTesseractSecret(secretName:String): UsernamePW = {
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
}

class AcroDb(jdbc:String, username:String, pw:String) {
  //WARNING this should be a singleton

  val config = new BoneCPConfig();	// create a new configuration object
  config.setJdbcUrl(jdbc);	// set the JDBC url
  config.setUsername(username);			// set the username
  config.setPassword(pw);				// set the password

  val connectionPool = new BoneCP(config); 	// setup the connection pool


  def getConnection(): Connection = {
    connectionPool.getConnection
  }


  def getConnection(autocommit: Boolean = true): Connection = {
    val connection = this.getConnection
    connection.setAutoCommit(autocommit)
    connection
  }


  def withConnection[A](block: Connection ⇒ A): A = {
    val connection = getConnection
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }
}