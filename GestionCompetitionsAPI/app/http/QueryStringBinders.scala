package http

import play.api.mvc.QueryStringBindable
import play.Logger
import play.api.mvc.PathBindable

object QueryStringBinders {

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) =
    new QueryStringBindable[Seq[String]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Seq[String]]] =
        stringBinder.bind(key, params).map(_.right.map(_.split(",").toList))

      override def unbind(key: String, strings: Seq[String]): String =
        s"""$key=${strings.mkString(",")}"""
    }
}