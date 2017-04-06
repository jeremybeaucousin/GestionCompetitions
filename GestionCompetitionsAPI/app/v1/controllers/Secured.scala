package v1.controllers

import play.api.mvc._
import v1.bo.Person
import v1.constantes.HttpConstants
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.Logger
import v1.http.ApiToken

trait Secured {

  def apiKeyOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.apiKey)

  def authTokenOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.authToken)

  def onUnauthorized(request: RequestHeader): Result = Results.Forbidden

  // TODO SEE why comments code do not work
  def withToken(apiToken: ApiToken => Request[AnyContent] => Future[Result]) = {
    Action.async(
      request => {
        val apiKeyOption = (apiKeyOpt(request))
        val authTokenOption = (authTokenOpt(request))
        if (apiKeyOption.isDefined && ApiToken.apiKeysExists(apiKeyOption.get) && authTokenOption.isDefined) {
          val futureToken = ApiToken.findByTokenAndApiKey(authTokenOption.get, apiKeyOption.get)
          //          futureToken.map(tokenOption => {
          //            if (tokenOption.isDefined && !tokenOption.get.isExpired) {
          //              val apiTokenFound = tokenOption.get
          //              ApiToken.raiseTokenDuration(apiTokenFound)
          //              apiToken(apiTokenFound)(request)
          //            } else {
          //              onUnauthorized(request)
          //            }
          //          })
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
      })
  }
}