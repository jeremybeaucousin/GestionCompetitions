package v1.dal.repos

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSON
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter
import v1.model.Person
import v1.utils.MongoDbUtil
import play.Logger

trait PersonRepo[T] {

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int]

  def find(personOption: Option[Person], searchInValues: Option[Boolean], sortOption: Option[Seq[String]], fieldsOption: Option[Seq[String]], offsetOption: Option[Int], limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]]

  def select(id: String, fieldsOption: Option[Seq[String]])(
    implicit bSONDocumentReader: BSONDocumentReader[T],
    bSONDocumentWriter: BSONDocumentWriter[T]): Future[Option[T]]

  /**
   * Update the person and send back the insciption result.
   * @param id
   * @param person
   * @return
   */
  def update(id: String, person: Person): Future[Boolean]

  /**
   * Delete the person and send back the insciption result.
   * @param id
   * @param person
   * @return
   */
  def remove(id: String): Future[Boolean]

  /**
   * Send back the inserted person if no error
   * @param person
   * @param bSONDocumentReader
   * @param bSONDocumentWriter
   * @return
   */
  def save(person: Person)(implicit bSONDocumentReader: BSONDocumentReader[T],
                           bSONDocumentWriter: BSONDocumentWriter[T]): Future[Boolean]
}

class PersonRepoImpl[T] @Inject() (val reactiveMongoApi: ReactiveMongoApi)(
  implicit ec: ExecutionContext)
    extends PersonRepo[T] {

  def collection = reactiveMongoApi.database.
    map(_.collection[BSONCollection]("persons"))

  override def find(
    personOption: Option[Person],
    searchInValues: Option[Boolean],
    sortOption: Option[Seq[String]],
    fieldsOption: Option[Seq[String]],
    offsetOption: Option[Int],
    limitOption: Option[Int])(implicit ec: ExecutionContext): Future[List[Person]] = {
    val personSearch: BSONDocument = if (personOption.isDefined) MongoDbUtil.constructBSONDocumentWithRootFields(BSON.write(personOption.get)) else BSONDocument()
    val valuesSearch: BSONDocument = if (searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch

    val sortBson = MongoDbUtil.createSortBson(sortOption)
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    val query = collection.map(_.find(valuesSearch).projection(projectionBson))
    val cursor = query.map(_.options(QueryOpts(skipN = MongoDbUtil.getSafeOffset(offsetOption))).sort(sortBson).cursor[Person]())
    cursor.flatMap(_.collect[List](limitOption.getOrElse(0)))
  }

  def getTotalCount(personOption: Option[Person], searchInValues: Option[Boolean]): Future[Int] = {
    val personSearch: BSONDocument = if (personOption.isDefined) BSON.write(personOption.get) else BSONDocument()
    val valuesSearch: BSONDocument = if (searchInValues.isDefined && searchInValues.get) MongoDbUtil.createSearchInValuesBson(personSearch) else personSearch
    collection.flatMap(_.count(Some(valuesSearch)))
  }

  override def select(id: String, fieldsOption: Option[Seq[String]])(
    implicit bSONDocumentReader: BSONDocumentReader[T],
    bSONDocumentWriter: BSONDocumentWriter[T]): Future[Option[T]] = {
    val projectionBson = MongoDbUtil.createProjectionBson(fieldsOption)
    collection.flatMap(_.find(constructId(id)).projection(projectionBson).one[T])
  }

  override def update(id: String, person: Person): Future[Boolean] = {
    val rebuildDocument = MongoDbUtil.constructBSONDocumentWithRootFields(BSON.write(person))
    val futureWriteResult = collection.flatMap(_.update(constructId(id), BSONDocument("$set" -> rebuildDocument)))
    handleWriteResult(futureWriteResult)
  }

  override def remove(id: String): Future[Boolean] = {
    val futureWriteResult = collection.flatMap(_.remove(constructId(id)))
    handleWriteResult(futureWriteResult)

  }

  override def save(person: Person)(implicit bSONDocumentReader: BSONDocumentReader[T],
                           bSONDocumentWriter: BSONDocumentWriter[T]): Future[Boolean] = {
    val futureWriteResult = collection.flatMap(_.insert(person))
    handleWriteResult(futureWriteResult)
  }

  private def constructId(id: String): BSONDocument = {
    BSONDocument("_id" -> id)
  }

  def handleWriteResult(futureWriteResult: Future[WriteResult]): Future[Boolean] = {
    futureWriteResult.map(writeResult => {
      !writeResult.hasErrors && writeResult.n > 0
    })
  }
}