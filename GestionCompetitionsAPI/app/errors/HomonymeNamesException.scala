package errors

import v1.constantes.MessageConstants
import play.api.i18n.MessagesApi

// TODO rethink that message importation
case class HomonymeNamesException (implicit messages: MessagesApi)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.sameNamesHomonyme)
}