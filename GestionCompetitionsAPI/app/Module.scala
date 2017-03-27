import com.google.inject.AbstractModule
import java.time.Clock
import java.time.Clock
import javax.inject.Singleton
import v1.services.Counter
import v1.services.ApplicationTimer
import v1.services.AtomicCounter
import v1.dal.PersonDAO
import v1.managers.PersonManager
import v1.managers.DocumentationManager

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bind(classOf[Counter]).to(classOf[AtomicCounter])
    bind(classOf[PersonDAO]).asEagerSingleton()
    bind(classOf[PersonManager]).asEagerSingleton()
    bind(classOf[DocumentationManager]).asEagerSingleton()
  }

}
