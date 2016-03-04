package chapter6

import scala.concurrent.duration._

import rx.lang.scala._

object Exercise2 extends App {
  val obs = Observable.interval(1.seconds).filter(s => (s % 5 == 0 || s % 12 == 0) && s % 30 != 0)

  obs.subscribe(println(_))
  Thread.sleep(90 * 1000)
}