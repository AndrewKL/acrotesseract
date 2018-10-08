package controllers


import javax.inject.Inject
import play.api.mvc.{Action, Controller}
import repo.{PosesAndTransitionsTrait, TestData, Transition}

class Transitions @Inject() (posesAndTransitions:PosesAndTransitionsTrait) extends Controller {
  def list = Action {
    Ok(views.html.transitions_list(posesAndTransitions.listTransitions()))
  }

  def get(transition_id: Int) = Action {
    posesAndTransitions.getTransition(transition_id) match {
      case Some(transition) => {
        val from = posesAndTransitions.getPose(transition.pose_from).get
        val to = posesAndTransitions.getPose(transition.pose_to).get
        Ok(views.html.transitions_transitions(transition,from,to))
      }
      case None => NotFound("Not found")
    }
  }
}
