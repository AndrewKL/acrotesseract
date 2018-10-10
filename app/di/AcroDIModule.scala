package di

import auth.GoogleAuthConfigUtils
import com.google.inject.{AbstractModule, Provides}
import com.gu.googleauth.{AntiForgeryChecker, AuthAction, GoogleAuthConfig}
import controllers.routes
import javax.inject.Singleton
import play.api.Configuration
import play.api.http.HttpConfiguration
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents}
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait, TestRepo}

import scala.concurrent.ExecutionContext

class AcroDIModule extends AbstractModule {
  override def configure(): Unit = {

    bind(classOf[PosesAndTransitionsTrait]).toInstance(new TestRepo)
  }

  @Provides
  @Singleton
  def googleAuthConfig(configuration:Configuration, httpConfiguration: HttpConfiguration):GoogleAuthConfig =
    GoogleAuthConfigUtils.googleAuthConfig(configuration,httpConfiguration)

  @Provides
  def authAction(googleAuthConfig: GoogleAuthConfig, executionContext: ExecutionContext,
                 components: ControllerComponents) = new AuthAction[AnyContent](
    googleAuthConfig,
    routes.Login.loginAction(),
    components.parsers.anyContent
  )(executionContext)

}
