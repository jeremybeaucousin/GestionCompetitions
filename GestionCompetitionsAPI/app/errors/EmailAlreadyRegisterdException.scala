package errors

import v1.constantes.MessageConstants
import javax.inject.Inject
import play.api.i18n.Messages

class EmailAlreadyRegisterdException (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.emailAlreadyRegistered)
}