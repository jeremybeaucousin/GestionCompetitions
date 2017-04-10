package v1.http

import java.io.PrintWriter
import java.io.StringWriter

import scala.concurrent._
import scala.concurrent.Future

import org.apache.http.HttpStatus

import v1.errors.BusinessException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import play.api._
import play.api.Logger
import play.api.http.DefaultHttpErrorHandler
import play.api.http.MediaRange
import play.api.http.MimeTypes
import play.api.http.Writeable
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.libs.json.JsResultException
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.Status
import play.api.routing.Router
import v1.constantes.MessageConstants
import org.apache.commons.lang3.StringUtils

@Singleton
class ErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router],
  implicit val messagesApi: MessagesApi)
    extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  implicit private var messages: Messages = null

  def messages_(requestHeader: RequestHeader): Unit = {
    messages = messagesApi.preferred(requestHeader)
  }

  override def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String) = {
    messages_(requestHeader)
    val userMessage = messages(MessageConstants.error.client, requestHeader.path)
    val error: Error = Error()
    error.code = Some(statusCode)
    error.userMessage = Some(userMessage)
    error.internalMessage = Some(message)
    if (getPrefferedContentType(requestHeader).accepts(MimeTypes.JSON)) {
      Future.successful(Status(statusCode)(Json.toJson(error)))
    } else {
      Future.successful(Status(statusCode)(v1.views.html.error(error)))
    }
  }

  override def onServerError(requestHeader: RequestHeader, exception: Throwable) = {
    messages_(requestHeader)
    val userMessage = messages(MessageConstants.error.server, requestHeader.path)

    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)

    var error: Error = Error()
    error.userMessage = Some(userMessage)
    error.internalMessage = Some(exception.getMessage)
    error.moreInfo = Some(sw.toString)

    var result = InternalServerError
    if (exception.isInstanceOf[BusinessException]) {
      error.code = Some(HttpStatus.SC_UNPROCESSABLE_ENTITY)
      result = UnprocessableEntity
      // Error coming from form validation
    } else if (exception.isInstanceOf[JsResultException]) {
      error.code = Some(HttpStatus.SC_UNPROCESSABLE_ENTITY)
      val jsError = exception.asInstanceOf[JsResultException]
      // Create the list containing the sub errors
      var errors: List[Error] = List[Error]()
      jsError.errors.foreach(jsError => {
        val subError = Error()
        // Add field
        subError.field = Some(jsError._1.toJsonString)
        jsError._2.foreach(details => {
          val sw = new StringWriter
          // The messages and arguments are in two differents lists
          for (i <- 0 to details.messages.size - 1) {
            val argument = if(details.args.isDefinedAt(i)) details.args(i) else StringUtils.EMPTY
            sw.append(messages(details.messages(i), argument))
          }
          subError.userMessage = Some(sw.toString())
        })
        errors = subError :: errors
      })
      error.errors = Some(errors)
      result = UnprocessableEntity
    } else {
      error.code = Some(HttpStatus.SC_INTERNAL_SERVER_ERROR)
      result = InternalServerError
    }

    if (getPrefferedContentType(requestHeader).accepts(MimeTypes.JSON)) {
      Future.successful(result(Json.toJson(error)))
    } else {
      Future.successful(result(v1.views.html.error(error)))
    }
  }

  private def getPrefferedContentType(requestHeader: RequestHeader): MediaRange = {
    val acceptedTypes = requestHeader.acceptedTypes
    if (!acceptedTypes.isEmpty) {
      acceptedTypes(0)
    } else {
      MediaRange.parse(MimeTypes.JSON)(0)
    }
  }
}