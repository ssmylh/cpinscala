package chapter7

import scala.concurrent.stm._

class TQueue[T] {
  private[this] val queue = Ref(scala.collection.immutable.Queue[T]())

  def enqueue(x: T)(implicit txn: InTxn): Unit = {
    queue() = queue().enqueue(x)
  }

  def dequeue()(implicit txn: InTxn): T = queue().dequeueOption match {
    case Some((x, q)) => {
      queue() = q
      x
    }
    case _ => retry
  }
}

object Exercise5 extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val latch = new java.util.concurrent.CountDownLatch(2)
  val q = new TQueue[Int]()
  Future {
    atomic { implicit txn =>
      val i = q.dequeue()
      assert(i == 1)
      latch.countDown()
    }
  }

  atomic { implicit txn =>
    q.enqueue(1)
  }

  latch.await()
}