package di

import auth.GoogleAuthConfigUtils
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
  def config():AcroConfig = AcroConfig.dev

  @Provides @Singleton
  def googleAuthConfig(config:AcroConfig, httpConfiguration: HttpConfiguration):GoogleAuthConfig =
    GoogleAuthConfigUtils.googleAuthConfig(config,httpConfiguration)

  @Provides
  def authAction(googleAuthConfig: GoogleAuthConfig, executionContext: ExecutionContext,
                 components: ControllerComponents) = new AuthAction[AnyContent](
    googleAuthConfig,
    routes.Login.loginAction(),
    components.parsers.anyContent
  )(executionContext)

  @Provides @Singleton
  def getAcroDb(config:AcroConfig):AcroDb = {
    val upw = getAcroTesseractSecret(secretName = config.dbPasswordSecretName)
    new AcroDb(jdbc = config.acrotesseractDevJDBC,username = upw.username,pw = upw.password)
  }
}
