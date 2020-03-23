package controllers

import com.iterable.play.utils.CaseClassMapping
import javax.inject.Inject
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, InjectedController}
import repo.{Pose, PosesAndTransitionsTrait, TestData, Transition}

class Graph @Inject() (posesAndTransitions:PosesAndTransitionsTrait) extends InjectedController {


  def index(focusPose:Option[Long]) = Action { implicit request =>

    Ok(views.html.graph()).withHeaders(("Cache-Control", "no-cache"))
  }

  def getData() = Action { implicit request =>
    implicit val p = Json.writes[Pose]
    implicit val t = Json.writes[Transition]
    implicit val pat = Json.writes[PosesAndTransistions]

    val resp = Json.toJson(PosesAndTransistions(
      posesAndTransitions.listPose(),
      posesAndTransitions.listTransitions())
    ).toString()
    Ok(resp).withHeaders(("Cache-Control", "no-cache"))
  }
}

case class PosesAndTransistions(poses:List[Pose], transitions:List[Transition])
