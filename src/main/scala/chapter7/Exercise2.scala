package chapter7

import scala.concurrent.stm._

class MVar[T] {
  private[this] var v: Ref[Option[T]] = Ref(None)
  def put(x: T)(implicit txn: InTxn): Unit =
    if (v().isEmpty) v() = Some(x)
    else retry

  def take()(implicit txn: InTxn): T = v() match {
    case Some(t) =>
      v() = None
      t
    case _ => retry
  }
}

object MVar {
  // On the condition that each method of `SyncVar` is thread safe,
  // it is impossible to implement `swap` atomically.
  def swap[T](a: MVar[T], b: MVar[T])(implicit txn: InTxn) = {
    val _a = a.take()
    a.put(b.take())
    b.put(_a)
  }
}

object Exercise2 extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val mvar1 = new MVar[Int]()
  atomic { implicit txn =>
    mvar1.put(1)
    val v1 = mvar1.take()
    assert(v1 == 1)
  }
  try {
    atomic.withRetryTimeout(100) { implicit txn =>
      mvar1.take()
    }
    assert(false, "should not reach this point.")
  } catch {
    case _: InterruptedException =>
    case _: Throwable => assert(false, "any exception should not be thrown except `InterruptedException`.")
  }

  val mvar2 = new MVar[Int]()
  @volatile var put = false
  Future {
    blocking {
      atomic { implicit txn =>
        val v2 = mvar2.take()
        assert(v2 == 2)
        assert(put)
      }
    }
  }
  Future {
    atomic { implicit txt =>
      mvar2.put(2)
      put = true
    }
  }

  val mvar3 = new MVar[Int]()
  @volatile var taken = false
  atomic { implicit txn =>
    mvar3.put(3)
  }
  Future {
    blocking {
      atomic { implicit txn =>
        mvar3.put(33)
        assert(taken)
      }
    }
  }
  Future {
    atomic { implicit txn =>
      val v3 = mvar3.take()
      assert(v3 == 3)
      taken = true
    }
  }

  val mvar4 = new MVar[Int]()
  val mvar5 = new MVar[Int]()
  atomic { implicit txn =>
    mvar4.put(1)
    mvar5.put(2)
    MVar.swap(mvar4, mvar5)
    assert(mvar4.take() == 2)
    assert(mvar5.take() == 1)
  }

  Thread.sleep(2000)
  println("end.")
}