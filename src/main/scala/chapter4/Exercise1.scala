package chapter4

import java.util.Timer
import java.util.TimerTask

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source
import scala.util._

object Exercise1 extends App {
  def timeout[T](timer: Timer, msec: Long, p: Promise[String]): Unit = {
    timer.schedule(new TimerTask {
      def run(): Unit = {
        p.trySuccess("timeout occurred.")
      }
    }, msec)
  }

  def printProgress(timer: Timer, intervalMsec: Long): Unit = {
    timer.schedule(new TimerTask {
      def run(): Unit = {
        print(".")
      }
    }, 0, intervalMsec)
  }

  while (true) {
    val url = io.StdIn.readLine
    val timer = new Timer(true)

    val p = Promise[String]
    val req = Future {
      timeout(timer, 2000, p)
      Source.fromURL(url).mkString
    } onComplete {
      case Success(s) => p.trySuccess(s)
      case Failure(e) => p.tryFailure(e)
    }

    printProgress(timer, 50)

    val result = Await.ready(p.future, duration.Duration.Inf)
    result.value.get match {
      case Success(html) => println("\n" + html)
      case Failure(e) => println("\n" + e)
    }
    timer.cancel()
  }
}