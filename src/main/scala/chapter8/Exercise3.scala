package chapter8

import akka.actor._
import akka.event.Logging
import akka.pattern._

class SessionActor(passowrd: String, r: ActorRef) extends Actor {
  import SessionActor._
  val log = Logging(context.system, this)

  def fowarding: Actor.Receive = {
    case EndSession => context.become(start)
    case message: String => r forward message
  }

  def start: Actor.Receive = {
    case StartSession(p) =>
      if (p == passowrd) context.become(fowarding)
      else log.info(s"password is not correct.")
    case _ =>
      log.info("send `StartSession(String)` message.")
  }
  override def receive: Actor.Receive = start
}

object SessionActor {
  case class StartSession(password: String)
  case object EndSession
  def props(password: String, r: ActorRef) = Props(classOf[SessionActor], password, r)
}

object Exercise3 extends App {
  import scala.concurrent._
  import scala.concurrent.duration._

  class EchoActor extends Actor {
    override def receive: Actor.Receive = {
      case message: String => sender ! message
      case _ => context.stop(self)
    }
  }

  lazy val exsys = ActorSystem("ExercisesSystem")

  val ea = exsys.actorOf(Props[EchoActor], "echo-actor")
  val sa = exsys.actorOf(SessionActor.props("abcde", ea), "session-actor")

  sa ! SessionActor.StartSession("abcde")
  implicit val timeout = akka.util.Timeout(1.seconds)
  val f1 = sa ? "test"
  assert(Await.result(f1, Duration.Inf) == "test")

  sa ! SessionActor.EndSession
  sa ! "foo"

  Thread.sleep(1000)
  exsys.terminate()
}