package v1.errors

import v1.constantes.MessageConstants
import play.api.i18n.Messages

case class HomonymNamesException (implicit val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.sameNamesHomonym)
}