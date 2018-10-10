package auth

import com.gu.googleauth.UserIdentity
import play.api.mvc.RequestHeader

object AllowedEditors {
  val allowed = List(
    "andrew.long.3001@gmail.com",
    "emilylisarose@gmail.com"
  )

  def isEditor(request: RequestHeader):Boolean ={
    UserIdentity.fromRequest(request) match {
      case None => return false
      case Some(id:UserIdentity) => return allowed.contains(id.email)
    }
  }

}
