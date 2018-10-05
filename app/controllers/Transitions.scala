package controllers


import play.api.mvc.{Action, Controller}
import repo.Transition

class Transitions extends Controller {
  def list = Action {
    Ok("transitions")
  }

  def get(transition_id:Int) = Action {
    Ok(s"transition_id: $transition_id")
  }
}
