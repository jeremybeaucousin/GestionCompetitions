package v1.services

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import play.api.mvc.Call

@Singleton
class GrantsServices @Inject() (
    implicit val ec: ExecutionContext) {

  final val ADMIN = "Admin"
  final val USER = "User"

  final val grantsScope = Map[String,List[Call]]()
}