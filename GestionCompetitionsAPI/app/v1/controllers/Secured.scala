package v1.controllers

import play.api.mvc._
import play.api.mvc.Results._
import v1.model.Person
import v1.constantes.HttpConstants
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import play.Logger
import v1.http.ApiToken
import scala.concurrent.Await

trait Secured {

  def apiKeyOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.xApiKey)

  def authTokenOpt(request: RequestHeader) = request.headers.get(HttpConstants.headerFields.xAuthToken)

  def onUnauthorized(request: RequestHeader, apiTokenOpt: Option[ApiToken]): Result = {
    if (apiTokenOpt.isDefined) {
      routes.AuthenticationController.signout()
    }
    Unauthorized
  }

  def withToken(apiToken: => ApiToken => Request[AnyContent] => Future[Result]): Action[AnyContent] = {
    withToken(BodyParsers.parse.default)(apiToken)
  }

  def withToken[A](bodyParser: BodyParser[A])(apiToken: => ApiToken => Request[A] => Future[Result]) = {
    Action.async(bodyParser) { request =>
      {
        Logger.info("withToken(bodyParser)")
        val apiKeyOption = (apiKeyOpt(request))
        val authTokenOption = (authTokenOpt(request))
        if (apiKeyOption.isDefined && ApiToken.apiKeysExists(apiKeyOption.get) && authTokenOption.isDefined) {
          val futureApiToken = ApiToken.findByTokenAndApiKey(authTokenOption.get, apiKeyOption.get)
          val apiTokenOpt = Await.result(futureApiToken, Duration.Inf)
          if (apiTokenOpt.isDefined && !apiTokenOpt.get.isExpired) {
            val apiTokenFound = apiTokenOpt.get
            val futureNewApiToken = ApiToken.create(apiTokenFound.apiKey, apiTokenFound.userId)
            val newApiToken = Await.result(futureApiToken, Duration.Inf)
            apiToken(newApiToken.get)(request)
          } else {
            Future(onUnauthorized(request, apiTokenOpt))
          }

        } else {
          Future(onUnauthorized(request, None))
        }
      }
    }
  }
}