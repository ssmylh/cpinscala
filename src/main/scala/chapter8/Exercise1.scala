package chapter8

import scala.concurrent._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor._
import akka.pattern._

class TimerActor extends Actor {
  import TimerActor._

  def receive = {
    case Register(t) =>
      val res = after(t.milliseconds, context.system.scheduler) { Future(Timeout) }
      res pipeTo sender
  }
}

object TimerActor {
  case class Register(t: Long)
  case object Timeout
}

object Exercise1 extends App {
  import TimerActor._

  lazy val exsys = ActorSystem("ExercisesSystem")

  val actor = exsys.actorOf(Props[TimerActor], "timer-actor")

  val results = for (i <- 1 to 10) yield {
    implicit val timeout = akka.util.Timeout(1500.milliseconds)
    actor ? Register(i * 100)
  }

  results.foreach { f => assert(Await.result(f, Duration.Inf) == Timeout) }

  exsys.terminate()
}