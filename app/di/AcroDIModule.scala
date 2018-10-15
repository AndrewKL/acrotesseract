package di

import auth.GoogleAuthConfigUtils
import com.google.inject.{AbstractModule, Provides}
import com.gu.googleauth.{AntiForgeryChecker, AuthAction, GoogleAuthConfig, UserIdentity}
import controllers.routes
import javax.inject.Singleton
import play.api.Configuration
import play.api.http.HttpConfiguration
import play.api.mvc.{AnyContent, BodyParser, ControllerComponents, RequestHeader}
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait, TestRepo}

import scala.concurrent.ExecutionContext

class AcroDIModule extends AbstractModule {
  override def configure(): Unit = {
    val dev = true

    bind(classOf[PosesAndTransitionsTrait]).toInstance(new TestRepo)


  }

  @Provides
  @Singleton
  def googleAuthConfig(configuration:Configuration, httpConfiguration: HttpConfiguration):GoogleAuthConfig =
    GoogleAuthConfig.withNoDomainRestriction(
      "clientid",
      "sec",
      "redir",
      antiForgeryChecker = AntiForgeryChecker.borrowSettingsFromPlay(httpConfiguration)
    )
  //GoogleAuthConfigUtils.googleAuthConfig(configuration,httpConfiguration)

  @Provides
  def authAction(googleAuthConfig: GoogleAuthConfig, executionContext: ExecutionContext,
                 components: ControllerComponents) = new AuthAction[AnyContent](
    googleAuthConfig,
    routes.Login.loginAction(),
    components.parsers.anyContent
  )(executionContext)

  @Provides
  def getUserFromRequest()/*request: RequestHeader)*/:Option[UserIdentity] =
    Option(UserIdentity("","email","firstname","lastname",1234,None))
    //UserIdentity.fromRequest(request)

}
