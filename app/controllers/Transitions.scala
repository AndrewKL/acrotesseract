package controllers


import com.iterable.play.utils.CaseClassMapping
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, Controller, InjectedController}
import repo.{PosesAndTransitionsTrait, TestData, Transition}

class Transitions @Inject() (posesAndTransitions:PosesAndTransitionsTrait) extends InjectedController {
  val transitionForm = Form(CaseClassMapping.mapping[UITransition])

  def list = Action {
    Ok(views.html.transitions_list(posesAndTransitions.listTransitions()))
  }

  def get(transition_id: Long) = Action {
    posesAndTransitions.getTransition(transition_id) match {
      case Some(transition) => {
        val from = posesAndTransitions.getPose(transition.pose_from).get
        val to = posesAndTransitions.getPose(transition.pose_to).get
        Ok(views.html.transitions_transitions(transition,from,to))
      }
      case None => NotFound("Not found")
    }
  }


  def getNewTransition() = Action { implicit request =>
    Ok(views.html.transitions_transition_new())
  }

  def postNewTransition() = Action { implicit request =>
    if(transitionForm.bindFromRequest().hasErrors) {
      val errors = transitionForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val transition = transitionForm.bindFromRequest().get.toTransition("TODO")
      val transitionId = posesAndTransitions.insertTransition(transition)
      Redirect(routes.Transitions.get(transitionId.toInt))
    }
  }

  def getEditTransition(transition_id:Long) = Action { implicit request =>
    posesAndTransitions.getTransition(transition_id) match {
      case Some(transition) => Ok(views.html.transitions_transition_edit(transition))
      case None => NotFound("Not found")
    }
  }

  def postEditTransition(transition_id:Long) = Action { implicit request =>
    if(transitionForm.bindFromRequest().hasErrors) {
      val errors = transitionForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val transition = transitionForm.bindFromRequest().get.toTransition("TODO")
      posesAndTransitions.updateTransition(transition)
      Redirect(routes.Transitions.get(transition_id.toInt))
    }
  }
}

case class UITransition(name:String,image_url:String,description_md:String,
                      pose_from:Int, pose_to:Int){
  def toTransition(createdBy:String) = new Transition(
    Option.empty,
    name,
    createdBy,
    image_url,
    description_md,
    pose_from,
    pose_to
  )
}
