package v1.controllers

import play.api.mvc.RequestHeader
import play.api.mvc.Results
import play.api.mvc.Security
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.AnyContent
import v1.bo.Person
import v1.constantes.HttpConstants
import models.ApiToken
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.Logger

trait Secured {

  def apiKeyOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.HEADER_API_KEY)

  def authTokenOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.HEADER_AUTH_TOKEN)

  def onUnauthorized(request: RequestHeader) = Results.Forbidden

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(authTokenOpt, onUnauthorized) { user =>
      Action.async(request => Future(f(user)(request)))
    }
  }

  // TODO Find do not work yet
  def withToken(apiToken: ApiToken => Request[AnyContent] => Result) = withAuth { authToken =>
    implicit request =>
      Logger.info(apiKeyOpt(request).get)
      Logger.info(authToken)
      if ((apiKeyOpt(request)).isDefined) {
        val futureToken = ApiToken.findByTokenAndApiKey(authToken, apiKeyOpt(request).get)
        val tokenTry = Await.ready(futureToken, Duration.Inf).value.get
        if (tokenTry.isSuccess && tokenTry.get.isDefined && tokenTry.get.get.isExpired) {
          val tokenTryOpt = tokenTry.get
          apiToken(tokenTryOpt.get)(request)
        } else {
          onUnauthorized(request)
        }
      } else {
        onUnauthorized(request)
      }
  }
}