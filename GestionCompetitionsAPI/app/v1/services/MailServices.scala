package v1.services

import scala.concurrent.ExecutionContext

import javax.inject.Inject
import javax.inject.Singleton
import play.api.i18n.Messages
import com.typesafe.plugin.MailerPlugin
import play.api.libs.mailer.Email
import play.api.libs.mailer.MailerClient
import scala.concurrent.Future
import play.Logger

@Singleton
class MailServices @Inject() (
    implicit val ec: ExecutionContext,
    val mailer: MailerClient) {

  def createAndSendEmail()(implicit messages: Messages) {
    val bodyHtml = Some(v1.views.html.mails.welcome().toString)
    val email = Email(subject = "subject", from = "jeremy.beaucousin@gmail.com", to = List("jeremy.beaucousin@gmail.com"), bodyHtml = bodyHtml, bodyText = Some("Hello"), replyTo = None)
    mailer.send(email)
    // TODO uncomment
    //Logger.info(email.toString())
//    val mail = MailerPlugin.email
//    mail.setSubject("subject")
//    mail.setFrom("jeremy.beaucousin@gmail.com")
//    mail.setRecipient("jeremy.beaucousin@gmail.com")
//    mail.sendHtml(bodyHtml)
  }
}
