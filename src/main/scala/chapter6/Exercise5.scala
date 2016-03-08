package chapter6

import rx.lang.scala._

class RCell[T] extends Signal[T] {
  private val subject = Subject[T]()
  setObservable(subject)

  def :=(x: T): Unit = {
    subject.onNext(x)
  }
}

object Exercise5 extends App {
  val seconds = new RCell[Int]
  val minutes = seconds.map(_ / 60)
  val hours = minutes.map(_ / 60)

  val sec1 = 1 * 60 * 60 + 2 * 60;
  seconds := sec1
  assert(seconds() == sec1)
  assert(minutes() == 60 + 2)
  assert(hours() == 1)

  val sec2 = 3 * 60 * 60 + 4 * 60
  seconds := sec2
  assert(seconds() == sec2)
  assert(minutes() == 3 * 60 + 4)
  assert(hours() == 3)

}