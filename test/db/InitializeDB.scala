package db

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import org.junit.{Ignore, Test}

import scala.util.parsing.json.JSON
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import repo.AcroDb
class InitializeDB {



  @Test
  @Ignore
  def migrateDB(): Unit = {
    val usernamePw = AcroDb.getAcroTesseractSecret()

    import org.flywaydb.core.Flyway
    // Create the Flyway instance and point it to the database// Create the Flyway instance and point it to the database

    val flyway: Flyway = Flyway.configure
      .dataSource(AcroDb.acrotesseractDevJDBC, usernamePw.username, usernamePw.password)
      .load

    // Start the migration
    flyway.repair()
    flyway.migrate()
  }

}

