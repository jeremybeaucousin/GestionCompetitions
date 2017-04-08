package v1.model
 
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import v1.constantes.ValidationConstants
import v1.constantes.MessageConstants

case class PasswordChange(
    val oldPassword: String,
    val newPasswordFirst: String,
    val newPasswordSecond: String)

  private object PasswordChange {

    final val OLD_PASSWORD = "oldPassword"
    final val NEW_PASSWORD_FIRST = "newPasswordFirst"
    final val NEW_PASSWORD_SECOND = "newPasswordSecond"

    implicit val passwordChangeBuilder: Reads[PasswordChange] = (
      (JsPath \ OLD_PASSWORD).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
      (JsPath \ NEW_PASSWORD_FIRST).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
      (JsPath \ NEW_PASSWORD_SECOND).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)))(PasswordChange.apply _)
  }