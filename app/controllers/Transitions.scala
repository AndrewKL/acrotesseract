package controllers


import com.iterable.play.utils.CaseClassMapping
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, Controller, InjectedController}
import repo.{PosesAndTransitionsTrait, TestData, Transition}

class Transitions @Inject() (posesAndTransitions:PosesAndTransitionsTrait) extends InjectedController {
  val transitionForm = Form(CaseClassMapping.mapping[UITransition])

  def list = Action { implicit request =>

    Ok(views.html.transitions_list(posesAndTransitions.listTransitions())).withHeaders(("Cache-Control", "no-cache"))
  }

  def get(transition_id: Long) = Action { implicit request =>
    posesAndTransitions.getTransition(transition_id) match {
      case Some(transition) => {
        val from = posesAndTransitions.getPose(transition.pose_from).get
        val to = posesAndTransitions.getPose(transition.pose_to).get
        Ok(views.html.transitions_transitions(transition,from,to))
      }
      case None => NotFound("Not found")
    }
  }

  def getNewTransition(from:Option[Long],to:Option[Long]) = Action { implicit request =>
    Ok(views.html.transitions_transition_new(from,to))
  }

  def postNewTransition() = Action { implicit request =>
    if(transitionForm.bindFromRequest().hasErrors) {
      val errors = transitionForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val transition = transitionForm.bindFromRequest().get.toTransition("TODO")
      val transitionId = posesAndTransitions.insertTransition(transition)
      Redirect(routes.Transitions.get(transitionId))
    }
  }

  def getEditTransition(transition_id:Long) = Action { implicit request =>
    posesAndTransitions.getTransition(transition_id) match {
      case Some(transition) => Ok(views.html.transitions_transition_edit(transition)).withHeaders(("Cache-Control", "no-cache"))
      case None => NotFound("Not found")
    }
  }

  def postEditTransition(transition_id:Long) = Action { implicit request =>
    if(transitionForm.bindFromRequest().hasErrors) {
      val errors = transitionForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val transition = transitionForm.bindFromRequest().get.toTransition("TODO")
      posesAndTransitions.updateTransition(transition.copy(transition_id = Option(transition_id)))
      Redirect(routes.Transitions.get(transition_id.toInt))
    }
  }

  def postDeleteTransition(transition_id:Long) = Action { implicit request =>
    posesAndTransitions.deleteTransition(transition_id)
    Redirect(routes.Transitions.list())
  }
}

case class UITransition(name:String,description_md:String,pose_from:Long, pose_to:Long,youtube_url:Option[String]){
  def toTransition(createdBy:String) = new Transition(
    Option.empty,
    name,
    createdBy,
    description_md,
    pose_from,
    pose_to,
    if(!youtube_url.isEmpty && youtube_url.get == "")Option.empty else youtube_url  //empty string is as good as empty
  )
}
