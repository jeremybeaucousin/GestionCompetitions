import java.time.Clock

import scala.concurrent.Future

import com.google.inject.AbstractModule
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.actor.ActorSystem
import javax.inject.Inject
import javax.inject.Singleton
import play.api.inject.ApplicationLifecycle
import v1.managers.DocumentationManager
import v1.services.ApplicationTimer
import v1.services.AtomicCounter
import v1.services.Counter
import models.ApiToken
import scala.concurrent.duration._

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bind(classOf[Counter]).to(classOf[AtomicCounter])
    bind(classOf[DocumentationManager]).asEagerSingleton()
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
