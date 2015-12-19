package chapter4

import scala.async.Async._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Exercise5 extends App {
  implicit class FutureOps[T](val self: Future[T]) {
    def exists(p: T => Boolean): Future[Boolean] = {
      async {
        p(await(self))
      }.recover {
        case _ => false
      }
    }
  }

  val f1 = Future { 10 }.exists(_ % 2 == 0)
  assert(Await.result(f1, Duration.Inf))

  val f2 = Future { 5 }.exists(_ % 2 == 0)
  assert(Await.result(f2, Duration.Inf) == false)

  val f3 = Future[Int] {
    throw new Exception
  }.exists(_ % 2 == 0)
  assert(Await.result(f3, Duration.Inf) == false)
}