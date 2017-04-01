package v1.controllers

import play.api.mvc._
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

  def onUnauthorized(request: RequestHeader): Result = Results.Forbidden

  def withAuth(f: => String => Request[AnyContent] => Future[Result]) = {
    Security.Authenticated(authTokenOpt, onUnauthorized) { authToken =>
      Action.async(request => f(authToken)(request))
    }
  }
  // Action.async { implicit request =>
  def withToken(apiToken: ApiToken => Request[AnyContent] => Future[Result]) = withAuth { authToken =>
    implicit request =>
      val apiKeyOption = (apiKeyOpt(request))
      if (apiKeyOption.isDefined && ApiToken.apiKeysExists(apiKeyOption.get)) {
        val futureToken = ApiToken.findByTokenAndApiKey(authToken, apiKeyOpt(request).get)
        //        futureToken.map(tokenOption => {
        //          if (tokenOption.isDefined && !tokenOption.get.isExpired) {
        //            val apiTokenFound = tokenOption.get
        //            ApiToken.raiseTokenDuration(apiTokenFound)
        //            apiToken(apiTokenFound)(request)
        //          } else {
        //            onUnauthorized(request)
        //          }
        //        })
        val apiTokenOpt = Await.ready(futureToken, Duration.Inf).value.get.get
        if (apiTokenOpt.isDefined && !apiTokenOpt.get.isExpired) {
          val apiTokenFound = apiTokenOpt.get
          ApiToken.raiseTokenDuration(apiTokenFound)
          apiToken(apiTokenFound)(request)
        } else {
          Future(onUnauthorized(request))
        }
      } else {
        Future(onUnauthorized(request))
      }
  }
}