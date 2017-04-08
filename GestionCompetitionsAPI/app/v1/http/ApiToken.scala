package v1.http

import org.joda.time.DateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.Map
import play.Logger
import scala.concurrent.duration.Duration
import v1.utils.SeqUtil
import v1.utils.SecurityUtil
import org.apache.commons.lang3.StringUtils

/*
* Stores the Auth Token information. Each token belongs to a Api Key and user
*/
case class ApiToken(
    var token: String = StringUtils.EMPTY, // UUID 36 digits
    var apiKey: String = StringUtils.EMPTY,
    var expirationTime: DateTime = null,
    var userId: String = StringUtils.EMPTY) {
  def isExpired = expirationTime.isBeforeNow
}

object ApiToken {

  final val TOKEN_FIELD = "token"
  final val DURATION_FIELD = "minutes"
  final val API_TOKEN_DURATION = 1

  val apiKeys = Map[String, String](
    "Web-App" -> "tkdhkd44")

  private var tokenStore: Seq[ApiToken] = Seq[ApiToken]()

  private def getExpirationTime = (new DateTime()) plusMinutes API_TOKEN_DURATION

  def apiKeysExists(apiKey: String): Boolean = apiKeys.values.exists(key => key.equals(apiKey))

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = Future.successful {
    val tokenResult = tokenStore.find(tokenResult => tokenResult.token.equals(token) && tokenResult.apiKey.equals(apiKey))
    tokenResult
  }

  def findByUserID(userId: String): Future[Option[ApiToken]] = Future.successful {
    val tokenResult = tokenStore.find(tokenResult => tokenResult.userId.equals(userId))
    tokenResult
  }

  def create(apiKey: String, userId: String): Future[ApiToken] = {
    val futureExistingApiToken = findByUserID(userId)
    futureExistingApiToken.map(apiToken => {
      if (apiToken.isDefined) {
        delete(apiToken.get)
      }
      val newApiToken = ApiToken(generateToken, apiKey, expirationTime = getExpirationTime, userId)
      tokenStore = tokenStore :+ newApiToken
      Logger.info(tokenStore.size.toString())
      tokenStore.foreach(apiToken => {
        Logger.info(apiToken.userId.toString())
        Logger.info(apiToken.token.toString())
      })
      newApiToken
    })
  }

  def delete(apiToken: ApiToken) = {
    val futurApiToken = findByTokenAndApiKey(apiToken.token, apiToken.apiKey)
    futurApiToken.map(apiTokenFound => {
      if (apiTokenFound.isDefined) {
        tokenStore = SeqUtil.removeElementFromSeq(apiTokenFound.get, tokenStore)
      }
      Logger.info(tokenStore.size.toString())
      tokenStore.foreach(apiToken => {
        Logger.info(apiToken.userId.toString())
        Logger.info(apiToken.token.toString())
      })
    })
  }

  def generateToken: String = {
    val uuid = SecurityUtil.generateUUID();
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
