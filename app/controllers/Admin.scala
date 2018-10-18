package controllers


import com.gu.googleauth.{AuthAction, UserIdentity}
import play.api._
import javax.inject.Inject
import play.api.mvc._
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait}
import auth.AllowedEditors._
import di.AcroConfig

class Admin @Inject() (repo:PosesAndTransitionsTrait, authAction: AuthAction[AnyContent], config:AcroConfig) extends InjectedController  {
  def index = authAction { request: RequestHeader =>
    if(!isEditor(request)) Unauthorized
    else {
      val user: Option[UserIdentity] = UserIdentity.fromRequest(request)
      Ok(views.html.admin(
        repo.countPoses(),
        repo.countTransitions(),
        user.get.asJson,
        isEditor(request),
        config.jdbcString
      ))
    }
  }

  def ping = authAction { request: RequestHeader =>
    Ok("OK")
  }
}
