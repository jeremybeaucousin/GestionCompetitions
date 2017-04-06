package v1.services

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import javax.inject.Singleton

@Singleton
class MailServices @Inject() (
    implicit val ec: ExecutionContext) {

}