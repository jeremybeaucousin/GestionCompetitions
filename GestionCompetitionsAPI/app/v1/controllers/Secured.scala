package v1.controllers

import play.api.mvc._
import v1.model.Person
import v1.constantes.HttpConstants
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import play.Logger
import v1.http.ApiToken

trait Secured {

  def apiKeyOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.apiKey)

  def authTokenOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.authToken)

  def onUnauthorized(request: RequestHeader, apiTokenOpt: Option[ApiToken]): Result = {
    if (apiTokenOpt.isDefined) {
      routes.AuthenticationController.signout()
    }
    Results.Unauthorized
  }

  def withToken(apiToken: => ApiToken => Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    Logger.info("withToken()")
    withToken(BodyParsers.parse.default)(apiToken)
  }
  
  def withToken[A](bodyParser: BodyParser[A])(apiToken: => ApiToken => Request[A] => Future[Result]) = {
    Action.async(bodyParser)(request => {
      Logger.info("withToken(bodyParser)")
      val apiKeyOption = (apiKeyOpt(request))
      val authTokenOption = (authTokenOpt(request))
      if (apiKeyOption.isDefined && ApiToken.apiKeysExists(apiKeyOption.get) && authTokenOption.isDefined) {
        val futureApiToken = ApiToken.findByTokenAndApiKey(authTokenOption.get, apiKeyOption.get)
        futureApiToken.flatMap(apiTokenOpt => {
          if (apiTokenOpt.isDefined && !apiTokenOpt.get.isExpired) {
            val apiTokenFound = apiTokenOpt.get
            val futureNewApiToken = ApiToken.create(apiTokenFound.apiKey, apiTokenFound.userId)
            futureNewApiToken.flatMap(newApiToken => {
              apiToken(newApiToken)(request)
            })
          } else {
            Future(onUnauthorized(request, apiTokenOpt))
          }
        })

      } else {
        Future(onUnauthorized(request, None))
      }
    })
  }
}