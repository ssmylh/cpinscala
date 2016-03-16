package chapter6

import rx.lang.scala._

class RMap[K, V] {
  private[this] val map = scala.collection.mutable.Map[K, Subject[V]]()

  def update(k: K, v: V): Unit = map.get(k) match {
    case Some(s) => s.onNext(v)
    case _ =>
      val s = Subject[V]()
      map(k) = s
      s.onNext(v)
  }

  /* This method throws `NoSuchElementException` if the key does not exist in the map. */
  def apply(k: K): Observable[V] = map.get(k).get
}

object Exercise6 extends App {
  import scala.collection.mutable.ListBuffer

  val rmap = new RMap[String, Int]()
  rmap("a") = 1

  val o = rmap("a")
  val buf = ListBuffer.empty[Int]
  o.subscribe(buf += _)

  rmap("a") = 2
  rmap("a") = 3

  assert(buf == ListBuffer(2, 3), buf)
}