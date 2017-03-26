import javax.inject._
import play.api._
import play.api.http.HttpFilters
import play.api.mvc._

import filters.ExampleFilter

@Singleton
class Filters @Inject() (
  env: Environment,
  exampleFilter: ExampleFilter) extends HttpFilters {

  override val filters = {
    if (env.mode == Mode.Dev) Seq(exampleFilter) else Seq.empty
  }

}
