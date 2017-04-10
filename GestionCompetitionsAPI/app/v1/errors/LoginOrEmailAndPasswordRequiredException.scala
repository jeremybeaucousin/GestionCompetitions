package v1.errors

import play.api.i18n.Messages
import v1.constantes.MessageConstants

class LoginOrEmailAndPasswordRequiredException (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.accountAlreadyCreated)
}