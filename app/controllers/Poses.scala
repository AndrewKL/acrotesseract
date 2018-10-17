package controllers

import com.gu.googleauth.UserIdentity
import com.iterable.play.utils.CaseClassMapping
import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, Controller, Request}
import repo.{Pose, PosesAndTransitionsTrait, TestData}

class Poses @Inject() (posesAndTransitions:PosesAndTransitionsTrait)
  extends Controller  {
  val poseForm = Form(CaseClassMapping.mapping[UIPose])

  def list = Action { implicit request =>
    Ok(views.html.poses_list(posesAndTransitions.listPose()))
  }

  def get(pose_id:Long) = Action { implicit request =>
    posesAndTransitions.getPose(pose_id) match {
      case Some(pose) => {
        val transitionsFrom = posesAndTransitions.listTransitionsFromPose(pose.pose_id.get)
        val transitionsTo = posesAndTransitions.listTransitionsToPose(pose.pose_id.get)
        Ok(views.html.poses_pose(pose,transitionsFrom,transitionsTo))
      }
      case None => NotFound("Not found")
    }
  }

  def getNewPose() = Action { implicit request =>
    Ok(views.html.poses_pose_new())
  }

  def postNewPose() = Action { implicit request =>
    if(poseForm.bindFromRequest().hasErrors) {
      val errors = poseForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val pose = poseForm.bindFromRequest().get.toPose("TODO")
      val poseId = posesAndTransitions.insertPose(pose)
      Redirect(routes.Poses.get(poseId))
    }
  }

  def getEditPose(pose_id:Long) = Action { implicit request =>
    posesAndTransitions.getPose(pose_id) match {
      case Some(pose) => Ok(views.html.poses_pose_add_edit(pose))
      case None => NotFound("Not found")
    }
  }

  def postEditPose(pose_id:Long) = Action { implicit request =>
    if(poseForm.bindFromRequest().hasErrors) {
      val errors = poseForm.bindFromRequest().errors.map(_.message).mkString(" ")
      BadRequest(s"BAD REQUEST: ${errors}")
    } else {
      val pose = poseForm.bindFromRequest().get.toPose("TODO")
      posesAndTransitions.updatePose(pose)
      Redirect(routes.Poses.get(pose_id))
    }
  }
}

case class UIPose(name:String,image_url:String,description_md:String){
  def toPose(createdBy:String) = new Pose(pose_id = Option.empty,name,createdBy,Option(image_url),Option(description_md))
}
