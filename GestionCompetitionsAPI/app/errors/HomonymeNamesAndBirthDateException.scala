package errors

import v1.constantes.MessageConstants
import javax.inject.Inject
import play.api.i18n.MessagesApi

// TODO rethink that message importation
class HomonymeNamesAndBirthDateException (implicit messages: MessagesApi)
  extends BusinessException {
    override def getMessage = messages(MessageConstants.error.sameNamesAndbirthDateHomonyme)
}