package v1.http

import play.api.mvc.QueryStringBindable
import play.Logger
import play.api.mvc.PathBindable

object QueryStringBinders {

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) =
    new QueryStringBindable[Seq[String]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Seq[String]]] = {
        // Replace the first space caracter by a +
        // TODO to finalize
        //        val regex = """([ ])([\w]+)""".r
        //        var paramsValueWithoutSpace = scala.collection.mutable.Map[String, Seq[String]]()
        //        params.map(param => {
        //          val paramKey = param._1
        //          param.get(paramKey).map(values => {
        //            var valuesWithoutSpace = Seq[String]()
        //            values.map(value => {
        //              if (regex.pattern.matcher(value).matches) {
        //                valuesWithoutSpace = valuesWithoutSpace :+ value.replace(" ", "+")
        //              } else {
        //                valuesWithoutSpace = valuesWithoutSpace :+ value
        //              }
        //            })
        //            paramsValueWithoutSpace = paramsValueWithoutSpace += (param._1 -> valuesWithoutSpace)
        //          })
        //        })
        //
        //        Logger.info(key)
        //        Logger.info(params.toString())
        //        Logger.info(paramsValueWithoutSpace.toString())
        // paramsValueWithoutSpace.toMap
        stringBinder.bind(key, params).map(_.right.map(_.split(",").toList))
      }

      override def unbind(key: String, strings: Seq[String]): String =
        s"""$key=${strings.mkString(",")}"""
    }
}