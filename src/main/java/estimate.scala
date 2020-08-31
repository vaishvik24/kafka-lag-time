import java.util

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.immutable._
import scala.collection.mutable

object estimate {

  def lookup(offset: Double, points: util.LinkedList[point] ) = {

      val (left, right) =
        points
        .reverseIterator
        .sliding(size = 2, step = 1)
        .map { case r :: l :: Nil => (l, r) }
          .find { case (l, r) => offset >= l.getOffset && offset <= r.getOffset }
        .getOrElse {
          (points.head, points.last)
        }
      println("left point")
      left.printPoint()
      println("right point")
      right.printPoint()
      val dx = (right.getTime - left.getTime).toDouble
      val dy = (right.getOffset - left.getOffset).toDouble
      val Px = (right.getTime).toDouble
      val Dy = (right.getOffset - offset).toDouble

      val ans = (Px - Dy * dx / dy);
      println("Estimated value in time : " + ans)
      ans
    }


}

