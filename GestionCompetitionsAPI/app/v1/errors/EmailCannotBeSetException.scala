package v1.errors

import v1.constantes.MessageConstants
import javax.inject.Inject
import play.api.i18n.Messages

class EmailCannotBeSetException (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.emailCannotBeSet)
}