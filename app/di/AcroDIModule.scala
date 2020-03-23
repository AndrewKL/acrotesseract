package di

import auth.GoogleAuthConfigUtils
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.secretsmanager.{AWSSecretsManager, AWSSecretsManagerClientBuilder}
import com.google.inject.{AbstractModule, Provides}
import com.gu.googleauth.{AuthAction, GoogleAuthConfig}
import controllers.routes
import javax.inject.Singleton
import play.api.http.HttpConfiguration
import play.api.mvc.{AnyContent, ControllerComponents}
import repo.AcroDb.getAcroTesseractSecret
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait}

import scala.concurrent.ExecutionContext

class AcroDIModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PosesAndTransitionsTrait]).to(classOf[PosesAndTransitionsRepo])
  }

  @Provides @Singleton
  def config(environment: play.Environment ):AcroConfig = {
    if(environment.isDev || environment.isTest){
      AcroConfig.dev
    } else {
      AcroConfig.prod
    }
  }

  @Provides @Singleton
  def credProvider(config:AcroConfig):AWSCredentialsProvider = config.profileCredentialsName match {
    case Some(profile) => new ProfileCredentialsProvider(profile)
    case _ =>new DefaultAWSCredentialsProviderChain()
  }

  @Provides @Singleton
  def googleAuthConfig(config:AcroConfig, httpConfiguration: HttpConfiguration,creds:AWSCredentialsProvider):GoogleAuthConfig =
    GoogleAuthConfigUtils.googleAuthConfig(config,httpConfiguration,creds)

  @Provides
  def authAction(googleAuthConfig: GoogleAuthConfig, executionContext: ExecutionContext,
                 components: ControllerComponents) = new AuthAction[AnyContent](
    googleAuthConfig,
    routes.Login.loginAction(),
    components.parsers.anyContent
  )(executionContext)

  @Provides @Singleton
  def getAcroDb(config:AcroConfig,creds:AWSCredentialsProvider):AcroDb = {
    val upw = getAcroTesseractSecret(secretName = config.dbPasswordSecretName,creds)
    new AcroDb(jdbc = config.jdbcString,username = upw.username,pw = upw.password)
  }

  @Provides
  def secretsClient(creds:AWSCredentialsProvider):AWSSecretsManager =
    AWSSecretsManagerClientBuilder.standard().withCredentials(creds).withRegion("us-west-2").build
}
