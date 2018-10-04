package controllers


import play.api._
import javax.inject.Inject
import play.api.mvc._
import repo.{AcroDb, PosesAndTransistionsRepo}

class Admin @Inject() (repo:PosesAndTransistionsRepo) extends Controller  {
  def index = Action {
    Ok(views.html.admin(repo.countPoses(),repo.countTransitions()))
  }
}
