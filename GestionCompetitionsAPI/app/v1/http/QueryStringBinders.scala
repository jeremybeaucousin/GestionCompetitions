package v1.http

import play.api.mvc.QueryStringBindable
import play.Logger
import play.api.mvc.PathBindable
import v1.constantes.HttpConstants

object QueryStringBinders {

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) =
    new QueryStringBindable[Seq[String]] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Seq[String]]] = {
        // Replace the first space caracter by a +
        var paramsCopy = collection.mutable.Map[String, Seq[String]]() ++= params
        if (HttpConstants.queryFields.sort.equals(key)) {
          val sortParams: Seq[String] = params.get(key).get
          paramsCopy = paramsCopy -= key
          var sortParamsWithoutSpace: Seq[String] = Seq[String]()
          // Test if the chain contains a backspace in beginning
          val regex = """[[,]?([ ])([\w]+)]*""".r
          sortParams.foreach(value => {
            if (regex.pattern.matcher(value).matches) {
              sortParamsWithoutSpace = sortParamsWithoutSpace :+ value.replace(" ", "+")
            } else {
              sortParamsWithoutSpace = sortParamsWithoutSpace :+ value
            }
          })
          paramsCopy = paramsCopy += (key -> sortParamsWithoutSpace)
        }
        stringBinder.bind(key, paramsCopy.toMap).map(_.right.map(_.split(",").toList))
      }

      override def unbind(key: String, strings: Seq[String]): String =
        s"""$key=${strings.mkString(",")}"""
    }
}