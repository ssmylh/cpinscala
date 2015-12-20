package chapter4

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

object Exercise8 extends App {
  implicit class PromiseOps[T](self: Promise[T]) {
    def compose[S](f: S => T): Promise[S] = {
      val p = Promise[S]
      p.future.onComplete {
        case Success(s) => self.trySuccess(f(s))
        case Failure(e) => self.tryFailure(e)
      }
      p
    }
  }

  {
    val p1 = Promise[Int]
    val p2: Promise[Int] = p1.compose(_ * 2)
    p2.success(2)
    assert(Await.result(p1.future, Duration.Inf) == 4)
  }

  {
    val p1 = Promise[Int]
    p1.success(1)
    val p2: Promise[Int] = p1.compose(_ * 2)
    p2.success(2)
    assert(Await.result(p1.future, Duration.Inf) == 1)
  }
}