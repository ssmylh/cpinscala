package chapter4

import scala.concurrent._
import duration.Duration
import ExecutionContext.Implicits.global

object Exercise3 extends App {
  implicit class FutureOps[T](val self: Future[T]) {
    def exists(p: T => Boolean): Future[Boolean] = self.map(p)
  }

  val f1 = Future { 10 }.exists(_ % 2 == 0)
  assert(Await.result(f1, Duration.Inf))

  val f2 = Future { 5 }.exists(_ % 2 == 0)
  assert(Await.result(f2, Duration.Inf) == false)
}