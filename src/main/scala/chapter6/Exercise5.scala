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
  val elapsedSeconds = new RCell[Int]
  val elapsedMinutes = elapsedSeconds.map(_ / 60)
  val elaplsedHours = elapsedMinutes.map(_ / 60)

  val sec1 = 1 * 60 * 60 + 2 * 60;
  elapsedSeconds := sec1
  assert(elapsedSeconds() == sec1)
  assert(elapsedMinutes() == 60 + 2)
  assert(elaplsedHours() == 1)

  val sec2 = 3 * 60 * 60 + 4 * 60
  elapsedSeconds := sec2
  assert(elapsedSeconds() == sec2)
  assert(elapsedMinutes() == 3 * 60 + 4)
  assert(elaplsedHours() == 3)

}