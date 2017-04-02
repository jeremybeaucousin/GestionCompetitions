package v1.http

import java.io.PrintWriter
import java.io.StringWriter

import scala.concurrent.Future

import org.apache.http.HttpStatus

import errors.BusinessException
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.http.MediaRange
import play.api.http.MimeTypes
import play.api.i18n.I18nComponents
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.Status
import v1.constantes.MessageConstants
import scala.concurrent.ExecutionContext
import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._
import javax.inject.Provider
import play.api.i18n.MessagesApi
import play.api.http.Writeable
import com.fasterxml.jackson.annotation.JsonValue
import play.api.libs.json.JsResultException

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
    var errors: List[Error] = List[Error]()
    val error: Error = new Error(
      Some(statusCode),
      Some(userMessage),
      Some(message),
      None)
    errors = error :: errors
    val preferedContentType = getPrefferedContentType(requestHeader)
    if (preferedContentType.accepts(MimeTypes.JSON)) {
      Future.successful(Status(statusCode)(Json.obj("errors" -> errors)))
    } else {
      Future.successful(Status(statusCode)(v1.views.html.error(errors)))
    }
  }

  override def onServerError(requestHeader: RequestHeader, exception: Throwable) = {
    messages_(requestHeader)
    val userMessage = messages(MessageConstants.error.server, requestHeader.path)
    var errors: List[Error] = List[Error]()

    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)

    val error: Error = Error()
    error.userMessage = Some(userMessage)
    error.internalMessage = Some(exception.getMessage)
    error.moreInfo = Some(sw.toString)

    errors = error :: errors

    val preferedContentType = getPrefferedContentType(requestHeader)

    def result[T](contents: T)(implicit writeable: Writeable[T]) = {
      error.code = Some(HttpStatus.SC_BAD_REQUEST)
      if (exception.isInstanceOf[BusinessException]) {
        Future.successful(BadRequest(contents))
      } else if (exception.isInstanceOf[JsResultException]) {
        val jsError = exception.asInstanceOf[JsResultException]
        jsError.errors.foreach(jsError => {
          Logger.info(jsError._1.toJsonString)
          jsError._2.foreach(fields => {
            Logger.info(fields.toString())
            Logger.info("args : " + fields.args.toString())
            Logger.info("message : " + fields.message.toString())
            Logger.info("messages : " + fields.messages.toString())
          })
        })
        Future.successful(BadRequest(jsError.toString()))
      } else {
        error.code = Some(HttpStatus.SC_INTERNAL_SERVER_ERROR)
        Future.successful(InternalServerError(contents))
      }
    }

    if (preferedContentType.accepts(MimeTypes.JSON)) {
      result(Json.obj("errors" -> errors))
    } else {
      result(v1.views.html.error(errors))
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