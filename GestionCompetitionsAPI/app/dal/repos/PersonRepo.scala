package dal.repos

import javax.inject.Inject
import bo.Person
import play.api.libs.json.{ JsObject, Json }
import play.api.Logger
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.collections.bson
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{ BSON, BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }
import utils.MongoDbUtil
import reactivemongo.bson.BSONWriter


trait PersonRepo {
  def find(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]]

  def select(id: String)(implicit ec: ExecutionContext): Future[Option[Person]]

  def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(id: String)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(person: Person)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PersonRepoImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext)
    extends PersonRepo with MongoController with ReactiveMongoComponents {

  def collection = reactiveMongoApi.database.
    map(_.collection[BSONCollection]("persons"))

  override def find(sort: Option[Seq[String]], fields: Option[Seq[String]], offset: Option[Int], limit: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]] = {
    val sortBson = createSortBson(sort)
    val cursor = collection.map(_.find(Json.obj()).sort(sortBson).cursor[Person]())
    cursor.flatMap(_.collect[List]()).map { persons =>
      persons
    }
  }

  override def select(id: String)(implicit ec: ExecutionContext): Future[Option[Person]] = {
    collection.flatMap(_.find(constructId(id)).one[Person])
  }

  override def update(id: String, person: Person)(implicit ec: ExecutionContext): Future[WriteResult] = {
    person._id=Some(id)
    val rebuildDocument = MongoDbUtil.constructBSONDocumentForPartialUpdate(BSON.write(person))
    collection.flatMap(_.update(constructId(id), BSONDocument("$set" -> rebuildDocument)))
  }

  override def remove(id: String)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.flatMap(_.remove(constructId(id)))
  }

  override def save(person: Person)(implicit ec: ExecutionContext): Future[WriteResult] = {
    person._id = Some(MongoDbUtil.generateId().stringify)
    collection.flatMap(_.insert(person))
  }
  
  private def constructId(id: String):BSONDocument = {
    BSONDocument("_id" -> id)
  }
  
  private def createSortBson(fields: Option[Seq[String]]):BSONDocument = {
    var sortingBson = BSONDocument()
    val regex = """(\-|\+)?([\w ]+)""".r
    if (fields.isDefined && !fields.isEmpty) {
      fields.get.map(field => {
        if(regex.pattern.matcher(field).matches) {
          field match {
            case regex(order, field) => {
              val mongoOrder = if (order != null && order == "-") -1 else 1
              sortingBson ++= (field -> mongoOrder)
            }
          }
        }
      })
    }
    sortingBson
  }

}