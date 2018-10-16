package repo

case class Transition(transition_id:Option[Int],name:String,created_by:String,image_url:String,description_md:String,
                      pose_from:Int, pose_to:Int)
