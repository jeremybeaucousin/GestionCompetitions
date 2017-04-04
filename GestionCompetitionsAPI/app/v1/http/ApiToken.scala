package v1.http

import org.joda.time.DateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.Map
import play.Logger
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import v1.utils.SeqUtil

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

  final val TOKEN_FIELD = "token"
  final val DURATION_FIELD = "minutes"
  final val TOKEN_DURATION = 30

  val apiKeys = Map[String, String](
    "Web-App" -> "tkdhkd44")

  private var tokenStore: Seq[ApiToken] = Seq[ApiToken]()

  private def setDuration = (new DateTime()) plusMinutes TOKEN_DURATION

  def apiKeysExists(apiKey: String): Boolean = apiKeys.values.exists(key => key.equals(apiKey))

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = {
    val tokenResult = tokenStore.find(tokenResult => tokenResult.token.equals(token) && tokenResult.apiKey.equals(apiKey))
    Future(tokenResult)
  }

  def raiseTokenDuration(apiToken: ApiToken) = {
    if (apiToken != null) {
      val index = tokenStore.indexOf(apiToken)
      val newApiToken = ApiToken(
        apiToken.token,
        apiToken.apiKey,
        expirationTime = setDuration, apiToken.userId)
      tokenStore = tokenStore.updated(index, newApiToken)
    }

  }

  def create(apiKey: String, userId: String): Future[String] = Future.successful {
    val token = generateToken
    tokenStore = tokenStore :+ ApiToken(token, apiKey, expirationTime = setDuration, userId)
    token
  }

  def delete(apiToken: ApiToken): Future[Unit] = Future.successful {
    val futurApiToken = findByTokenAndApiKey(apiToken.token, apiToken.apiKey)
    val apiTokenFound = Await.ready(futurApiToken, Duration.Inf).value.get.get
    if (apiTokenFound.isDefined) {
      tokenStore = SeqUtil.removeElementFromSeq(apiTokenFound.get, tokenStore)
    }
  }

  def generateToken: String = {
    val uuid = UUID.randomUUID().toString
    if (!tokenStore.exists(_.token == uuid)) uuid else generateToken
  }
  
  /**
   * Remove all expired token from the store
   */
  def cleanTokenStore = {
    def expiredToken = tokenStore.find(tokenResult => tokenResult.isExpired)
    while (expiredToken.isDefined) {
      tokenStore = SeqUtil.removeElementFromSeq(expiredToken.get, tokenStore)
    }
  }
}
