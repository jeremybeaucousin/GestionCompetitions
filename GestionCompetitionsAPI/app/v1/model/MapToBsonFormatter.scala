package v1.model

import reactivemongo.bson._

object MapToBsonFormatter {
  object MapWriter extends BSONDocumentWriter[Map[String, String]] {
    def write(map: Map[String, String]): BSONDocument = {
      var bson = BSONDocument()
      map.foreach(tuple2 => {
        bson ++= (tuple2._1 -> tuple2._2)
      })
      bson
    }
  }

  object MapReader extends BSONDocumentReader[Map[String, String]] {
    def read(bson: BSONDocument): Map[String, String] = {
      val map = scala.collection.mutable.Map[String, String]()
      bson.elements.foreach(element => {
        if (element._2.isInstanceOf[BSONString]) {
          val stringValue = element._2.asInstanceOf[BSONString]
          map += (element._1.toString() -> stringValue.value)
        }
      })
      map.toMap
    }

  }
}