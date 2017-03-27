package v1.http

import akka.util.Switch
import java.io.{ StringWriter, PrintWriter }
import javax.inject.Inject
import javax.inject.Singleton;
import org.apache.http.HttpStatus
import play.api.http.HttpErrorHandler
import play.api.http.MimeTypes
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import scala.util.parsing.json.JSONArray
import play.api.i18n.Lang
import v1.constantes.MessageConstant
import play.api.http.MediaRange

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi)(implicit ec: ExecutionContext)
    extends HttpErrorHandler with I18nSupport {

  def onClientError(requestHeader: RequestHeader, statusCode: Int, message: String) = {
    val messages = messagesApi.preferred(requestHeader)
    val userMessage = messages(MessageConstant.error.client, requestHeader.path)
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
      Future.successful(Status(statusCode)(v1.views.html.error(errors, messages)))
    }
  }

  def onServerError(requestHeader: RequestHeader, exception: Throwable) = {
    val messages = messagesApi.preferred(requestHeader)
    val userMessage = messages(MessageConstant.error.server, requestHeader.path)
    var errors: List[Error] = List[Error]()
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    Logger.error(sw.toString)
    val error: Error = new Error(
      Some(HttpStatus.SC_INTERNAL_SERVER_ERROR),
      Some(userMessage),
      Some(exception.getMessage),
      Some(sw.toString))
    errors = error :: errors
    val preferedContentType = getPrefferedContentType(requestHeader)
    if (preferedContentType.accepts(MimeTypes.JSON)) {
      Future.successful(InternalServerError(Json.obj("errors" -> errors)))
    } else {
      Future.successful(InternalServerError(v1.views.html.error(errors, messages)))
    }
  }

  private def getPrefferedContentType(requestHeader: RequestHeader):MediaRange = {
    val acceptedTypes = requestHeader.acceptedTypes
    if (!acceptedTypes.isEmpty) {
      acceptedTypes(0)
    } else {
      MediaRange.parse(MimeTypes.JSON)(0)
    }
  }

}