package models

import org.joda.time.DateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.Map
import play.Logger

/*
* Stores the Auth Token information. Each token belongs to a Api Key and user
*/
case class ApiToken(
    token: String, // UUID 36 digits
    apiKey: String,
    expirationTime: DateTime,
    userId: String) {
  def isExpired = expirationTime.isBeforeNow
}

object ApiToken {

  final val TOKEN_DURATION = 2  
  
  val apiKeys = Map[String, String](
    "sportsEventsManagerApi" -> "tkdhkd44")

  private var tokenStore: Seq[ApiToken] = Seq[ApiToken]()

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = {
    Logger.info(token)
    Logger.info(apiKey)
    Logger.info(tokenStore.toString())
    Future(tokenStore.find(tokenResult => tokenResult.token == token && tokenResult.apiKey == apiKey))
  }

  def create(apiKey: String, userId: String): Future[String] = Future.successful {
    // Be sure the uuid is not already taken for another token
    def newUUID: String = {
      val uuid = UUID.randomUUID().toString
      if (!tokenStore.exists(_.token == uuid)) uuid else newUUID
    }
    val token = newUUID
    tokenStore = tokenStore :+ ApiToken(token, apiKey, expirationTime = (new DateTime()) plusMinutes TOKEN_DURATION, userId)
    Logger.info(tokenStore.size.toString())
    token
  }

  def delete(token: String): Future[Unit] = Future.successful {
    tokenStore = tokenStore.filter(tokenResult => tokenResult.equals(token))
  }
}
