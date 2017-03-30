package errors

import v1.constantes.MessageConstants
import play.api.i18n.Messages

// TODO Rethink message  importation
class HomonymeNamesAndBirthDateException(val messages: Messages)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.sameNamesAndbirthDateHomonyme)
}