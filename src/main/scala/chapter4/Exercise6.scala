package chapter4

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.sys.process._

object Exercise6 extends App {
  def spawn(command: String): Future[Int] = Future {
    // avoids blocking in asynchronous computations,
    // since it causes thread starvation.
    blocking { Process(command).! }
  }

  val f1 = spawn("ls -l")
  assert(Await.result(f1, Duration.Inf) == 0)

  val start = System.nanoTime()
  val futures = for (_ <- 0 until 16) yield spawn("sleep 1")
  futures.foreach { future =>
    Await.ready(future, Duration.Inf)
  }
  val end = System.nanoTime()
  val totalMsec = (end - start) / 1000000
  assert(totalMsec < 2000, "time over")
}