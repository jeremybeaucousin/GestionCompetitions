
package v1.errors
import play.api.i18n.Messages
import v1.constantes.MessageConstants

class EmailPasswordLoginRequired (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.emailPasswordLoginRequired)
}