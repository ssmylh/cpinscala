package chapter6

import rx.lang.scala._

class RMap[K, V] {
  import scala.collection._
  private[this] val allSubscribers = mutable.Map[K, (Subject[V], mutable.Set[Subscriber[V]])]()
  private[this] val map = mutable.Map[K, V]()

  def update(k: K, v: V): Unit = {
    map(k) = v
    allSubscribers.get(k) match {
      case Some(s) => s._1.onNext(v)
      case _ =>
    }
  }

  def apply(k: K): Observable[V] = Observable[V] { subscriber =>
    val (subject, subscribers) =
      allSubscribers.getOrElseUpdate(k, (Subject[V](), mutable.Set.empty[Subscriber[V]]))
    subscribers += subscriber

    val subscription = subject.subscribe(subscriber)

    subscriber.add(Subscription {
      subscription.unsubscribe()

      subscribers -= subscriber
      if (subscribers.isEmpty) {
        allSubscribers -= k
      }
    })
  }

  /* return true if there is at least one subscriber which subscribes to the updates of the specific key. */
  def hasSubscribers(k: K): Boolean = allSubscribers.get(k).isDefined
}

object Exercise6 extends App {
  import scala.collection.mutable.ListBuffer

  val rmap = new RMap[String, Int]()

  val key = "a"
  val o = rmap(key)
  assert(rmap.hasSubscribers(key) == false)

  val buf1 = ListBuffer.empty[Int]
  val subscription1 = o.subscribe(buf1 += _)
  val buf2 = ListBuffer.empty[Int]
  val subscription2 = o.subscribe(buf2 += _)

  rmap(key) = 1
  rmap(key) = 2
  assert(buf1 == ListBuffer(1, 2), buf1)
  assert(buf2 == ListBuffer(1, 2), buf2)

  subscription1.unsubscribe()
  assert(rmap.hasSubscribers(key))
  subscription2.unsubscribe()
  assert(rmap.hasSubscribers(key) == false)
}