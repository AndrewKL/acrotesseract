package controllers

import javax.inject.Inject
import play.api.mvc.{Action, Controller}
import repo.{Pose, PosesAndTransitionsTrait, TestData}

class Poses @Inject() (posesAndTransitions:PosesAndTransitionsTrait) extends Controller  {
  def list = Action {
    Ok(views.html.poses_list(posesAndTransitions.listPose()))
  }

  def get(pose_id:Int) = Action {
    posesAndTransitions.getPose(pose_id) match {
      case Some(pose) => {
        val transitionsFrom = posesAndTransitions.listTransitionsFromPose(pose.pose_id.get)
        val transitionsTo = posesAndTransitions.listTransitionsToPose(pose.pose_id.get)
        Ok(views.html.poses_pose(pose,transitionsFrom,transitionsTo))
      }
      case None => NotFound("Not found")
    }
  }
}
