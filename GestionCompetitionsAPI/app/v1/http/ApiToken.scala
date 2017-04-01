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

  final val TOKEN_DURATION = 1

  val apiKeys = Map[String, String](
    "Web-App" -> "tkdhkd44")

  private var tokenStore: Seq[ApiToken] = Seq[ApiToken]()

  private def setDuration = (new DateTime()) plusMinutes TOKEN_DURATION
    
  def apiKeysExists(apiKey: String):Boolean = apiKeys.values.exists(key => key.equals(apiKey))
      
  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = {
    val tokenResult = tokenStore.find(tokenResult => tokenResult.token.equals(token) && tokenResult.apiKey.equals(apiKey))
    Future(tokenResult)
  }

  def raiseTokenDuration(apiToken: ApiToken) = {
    if(apiToken != null) {
      val index = tokenStore.indexOf(apiToken)
    val newApiToken = ApiToken(
        apiToken.token,
        apiToken.apiKey, 
        expirationTime = setDuration, apiToken.userId)
    tokenStore = tokenStore.updated(index, newApiToken)
    }
    
  }
  
  def create(apiKey: String, userId: String): Future[String] = Future.successful {
    def newUUID: String = {
      val uuid = UUID.randomUUID().toString
      if (!tokenStore.exists(_.token == uuid)) uuid else newUUID
    }
    val token = newUUID
    tokenStore = tokenStore :+ ApiToken(token, apiKey, expirationTime = setDuration, userId)
    Logger.info(tokenStore.size.toString())
    token
  }

  def delete(apiToken: ApiToken): Future[Unit] = Future.successful {
    Logger.info(tokenStore.size.toString())
    tokenStore = tokenStore.dropWhile(storedApiToken => storedApiToken.equals(apiToken))
    Logger.info(tokenStore.size.toString())
  }
}