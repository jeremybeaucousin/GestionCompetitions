package v1.utils

object SeqUtil {
  def removeElementFromSeq[T](element: T, sequence: Seq[T]): Seq[T] = {
    if (element != null && sequence != null && !sequence.isEmpty) {
      val elementIndex = sequence.indexOf(element)
      sequence.patch(elementIndex, Nil, 1)
    } else {
      sequence
    }

  }

}