package chapter4

import scala.concurrent._
import ExecutionContext.Implicits.global
import duration.Duration
import scala.util._

object Exercise4 extends App {
  implicit class FutureOps[T](val self: Future[T]) {
    def exists(p: T => Boolean): Future[Boolean] = {
      val promise = Promise[Boolean]
      self.onComplete {
        case Success(t) => promise.success(p(t))
        case Failure(_) => promise.success(false)
      }
      promise.future
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