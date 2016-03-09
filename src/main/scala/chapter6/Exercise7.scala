package chapter6

import rx.lang.scala._

class RPriorityQueue[T](implicit val ord: Ordering[T]) {
  private[this] val pq = new scala.collection.mutable.PriorityQueue[T]()(ord.reverse)
  private[this] val subject = Subject[T]()

  def add(x: T): Unit = {
    pq += x
  }

  /* This method throws `NoSuchElementException` if the queue is empty. */
  def pop(): T = {
    val x = pq.dequeue()
    subject.onNext(x)
    x
  }

  def popped: Observable[T] = subject
}

object Exercise7 extends App {
  val rqueue = new RPriorityQueue[Int]()
  val o = rqueue.popped

  var count = 1
  o.subscribe(i => count match {
    case 1 => assert(i == 1); count += 1
    case 2 => assert(i == 2); count += 1
    case 3 => assert(i == 3); count += 1
  })

  rqueue.add(3)
  rqueue.add(1)
  rqueue.add(2)

  assert(rqueue.pop() == 1)
  assert(rqueue.pop() == 2)
  assert(rqueue.pop() == 3)
}