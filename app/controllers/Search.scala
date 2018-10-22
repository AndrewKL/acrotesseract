package controllers

import javax.inject.Inject
import play.api.mvc.InjectedController
import repo.{Pose, PosesAndTransitionsTrait, Transition}
import play.api.libs.json._
import play.api.libs.functional.syntax._

class Search @Inject() (repo:PosesAndTransitionsTrait)
  extends InjectedController  {

  implicit val p = Json.writes[Pose]
  implicit val t = Json.writes[Transition]
  implicit val r: OWrites[SearchResp] = Json.writes[SearchResp]


  def search(searchText:Option[String]) = Action { implicit request =>

    Ok(Json.toJson(new SearchResp(
      repo.searchPose(searchText.getOrElse("")),
      repo.searchTransitions(searchText.getOrElse(""))
    )))
  }
}

case class SearchResp(poses:List[Pose],transitions:List[Transition])
