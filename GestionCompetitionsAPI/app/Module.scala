import java.time.Clock

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.google.inject.AbstractModule

import akka.actor.ActorSystem
import javax.inject.Inject
import javax.inject.Singleton
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import v1.services.DocumentationServices
import v1.http.ApiToken
import reactivemongo.bson.BSONDocumentReader
import v1.model.Person
import v1.model.Person.PersonReader
import reactivemongo.bson.BSONDocumentWriter
import v1.model.Person.PersonWriter

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[DocumentationServices]).asEagerSingleton()
    bind(classOf[RecurrentTask]).asEagerSingleton()
  }

}

@Singleton
class RecurrentTask @Inject() (actorSystem: ActorSystem, lifecycle: ApplicationLifecycle) {

  // Clean the expired token from the token store
  actorSystem.scheduler.schedule(0.second, 1.hour) {
    ApiToken.cleanTokenStore
  }

  lifecycle.addStopHook { () =>
    Future.successful(actorSystem.terminate())
  }
}
