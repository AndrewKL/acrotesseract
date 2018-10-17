package repo

case class Transition(transition_id:Option[Long],name:String,created_by:String,
                      description_md:String,pose_from:Long, pose_to:Long)
