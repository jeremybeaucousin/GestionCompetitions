package v1.utils

object OptUtils {
  def testIfElementAreEquals[T](firstElement: Option[T], secondElement: Option[T]): Boolean = {
    firstElement.isDefined && secondElement.isDefined && firstElement.get.equals(secondElement.get)
  }
}