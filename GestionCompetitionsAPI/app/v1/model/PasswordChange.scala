package v1.model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import v1.constantes.ValidationConstants
import v1.constantes.MessageConstants
import org.apache.commons.lang3.StringUtils

case class PasswordChange(
  val oldPassword: String = StringUtils.EMPTY,
  val newPasswordFirst: String = StringUtils.EMPTY,
  val newPasswordSecond: String = StringUtils.EMPTY)

private object PasswordChange {

  final val OLD_PASSWORD = "oldPassword"
  final val NEW_PASSWORD_FIRST = "newPasswordFirst"
  final val NEW_PASSWORD_SECOND = "newPasswordSecond"

  val passwordChangeReads: Reads[PasswordChange] = (
    (JsPath \ OLD_PASSWORD).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
    (JsPath \ NEW_PASSWORD_FIRST).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)) and
    (JsPath \ NEW_PASSWORD_SECOND).read[String](pattern(ValidationConstants.regex.PASSWORD, MessageConstants.error.password)))(PasswordChange.apply _)

  object PasswordChangeWrites extends Writes[PasswordChange] {
    def writes(passwordChange: PasswordChange): JsObject = {
      var json = Json.obj()
      json += (OLD_PASSWORD -> JsString(passwordChange.oldPassword))
      json += (NEW_PASSWORD_FIRST -> JsString(passwordChange.newPasswordFirst))
      json += (NEW_PASSWORD_SECOND -> JsString(passwordChange.newPasswordSecond))
      json
    }
  }

  implicit object PasswordChangeFormat extends Format[PasswordChange] {
    def reads(json: JsValue) = passwordChangeReads.reads(json)

    def writes(passwordChange: PasswordChange) = PasswordChangeWrites.writes(passwordChange)
  }
}