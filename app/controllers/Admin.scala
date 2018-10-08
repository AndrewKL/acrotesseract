package controllers


import play.api._
import javax.inject.Inject
import play.api.mvc._
import repo.{AcroDb, PosesAndTransitionsRepo, PosesAndTransitionsTrait}

class Admin @Inject() (repo:PosesAndTransitionsTrait) extends InjectedController  {
  def index = Action {
    Ok(views.html.admin(repo.countPoses(),repo.countTransitions()))
  }
}
