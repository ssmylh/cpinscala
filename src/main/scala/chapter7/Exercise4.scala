package chapter7

object Exercise4 extends App {
  import scala.concurrent.stm._

  def atomicWithRetryMax[T](n: Int)(block: InTxn => T): T = {
    var count = 0
    atomic { implicit txn =>
      Txn.afterRollback { _ =>
        count += 1
      }
      if (count >= n) throw new RuntimeException("the number of retry reached max.")
      block(txn)
    }
  }

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val t1 = atomicWithRetryMax(1) { txn => 1 }
  assert(t1 == 1)

  val r = Ref(0)
  Future {
    atomic { implicit txn =>
      Thread.sleep(250)
      r() = 1
    }
  }

  try {
    atomicWithRetryMax(1) { implicit txn =>
      val _r = r()
      Thread.sleep(500)
      _r + r()
    }
    assert(false, "should not reach this point.")
  } catch {
    case e: RuntimeException => assert(e.getMessage() == "the number of retry reached max.")
    case e: Throwable => assert(false, e.getStackTrace)
  }

}