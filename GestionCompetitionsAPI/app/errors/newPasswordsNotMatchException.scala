package errors

import play.api.i18n.Messages
import v1.constantes.MessageConstants

class PasswordsNotMatchException (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.newPasswordsDoesNotMatch)
}