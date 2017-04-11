package v1.controllers

import play.api.libs.oauth._
import play.api.mvc._
import play.api.mvc.Results._
import v1.constantes.HttpConstants

trait SecuredOAuth {
  val KEY = ConsumerKey("Web-App", "tkdhkd44")

  val oauth = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY),
    true)

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get(HttpConstants.SessionFields.TOKEN)
      secret <- request.session.get(HttpConstants.SessionFields.SECRET)
    } yield {
      RequestToken(token, secret)
    }
  }

   def authenticate2(returnResult: => RequestToken => Request[AnyContent] => Result): Action[AnyContent] = {
    authenticate2(BodyParsers.parse.default)(returnResult)
  }
    
  def authenticate2[A](bodyParser: BodyParser[A])(returnResult: => RequestToken => Request[A] => Result) = Action(bodyParser) { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      oauth.retrieveAccessToken(tokenPair, verifier) match {
        case Right(requestToken) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          returnResult(requestToken)(request)
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      oauth.retrieveRequestToken(routes.AuthenticationController.signin().url) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(oauth.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      })
  }
  
  def authenticate = Action { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      oauth.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          Redirect(routes.AuthenticationController.index).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      }
    }.getOrElse(
      oauth.retrieveRequestToken(routes.AuthenticationController.signin().url) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(oauth.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      })
  }
}