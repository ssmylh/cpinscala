package chapter8

import akka.actor._
import akka.event.Logging

class AccountActor(name: String, var money: Int) extends Actor {
  import AccountActor._
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case MinusMoney(m) =>
      if (money - m >= 0) {
        money -= m
        sender ! OK
      } else {
        sender ! Error
      }
    case PlusMoney(m) =>
      money += m
      sender ! OK
    case Output =>
      log.info(s"$name, $money")
  }
}

object AccountActor {
  case class MinusMoney(money: Int)
  case class PlusMoney(money: Int)
  case object OK
  case object Error
  case object Output

  def props(name: String, money: Int) = Props(classOf[AccountActor], name, money)
}

class TransferActor extends Actor {
  import TransferActor._
  val log = Logging(context.system, this)

  def start: Actor.Receive = {
    case Transfer(from, to, money) =>
      from ! AccountActor.MinusMoney(money)
      context.become(waitFrom(from, to, money))
  }

  def waitFrom(from: ActorRef, to: ActorRef, money: Int): Actor.Receive = {
    case AccountActor.OK =>
      to ! AccountActor.PlusMoney(money)
      context.become(waitTo(money))
    case AccountActor.Error =>
      log.error(s"$from has not have enough money.")
      context.stop(self)
  }

  def waitTo(money: Int): Actor.Receive = {
    case AccountActor.OK =>
      context.become(start)
  }

  override def receive: Actor.Receive = start
}

object TransferActor {
  case class Transfer(from: ActorRef, to: ActorRef, money: Int)
}

object Exercise2 extends App {
  lazy val exsys = ActorSystem("ExercisesSystem")

  val a1 = exsys.actorOf(AccountActor.props("a1", 1000), "a1")
  val a2 = exsys.actorOf(AccountActor.props("a2", 600), "a2")

  val t = exsys.actorOf(Props[TransferActor], "transfer")
  t ! TransferActor.Transfer(a1, a2, 300)

  Thread.sleep(1000)

  a1 ! AccountActor.Output
  a2 ! AccountActor.Output

  Thread.sleep(1000)

  exsys.terminate()
}