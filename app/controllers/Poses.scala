package controllers

import play.api.mvc.{Action, Controller}
import repo.Pose

class Poses extends Controller  {
  def list = Action {
    Ok(views.html.poses_list(List.empty))
  }

  def get(pose_id:Int) = Action {
    Ok(views.html.poses_pose(new Pose(Option(pose_id),"name","created_by","image_url","description")))
  }
}
