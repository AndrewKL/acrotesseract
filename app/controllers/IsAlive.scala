package controllers

import play.api._
import javax.inject.Inject
import play.api.mvc._

class IsAlive extends Controller {
  def isAlive() = Action { implicit request =>
    Ok("Ok").withHeaders(("Cache-Control", "no-cache"))
  }

}
