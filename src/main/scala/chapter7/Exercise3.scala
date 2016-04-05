package chapter7

object Exercise3 extends App {
  import scala.concurrent.stm._

  def atomicRollbackCount[T](block: InTxn => T): (T, Int) = {
    var count = 0
    atomic { implicit txn =>
      Txn.afterRollback(_ => count += 1)
      val t = block(txn)
      (t, count)
    }
  }

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val (t1, i1) = atomicRollbackCount { txn => 1 }
  assert(t1 == 1)
  assert(i1 == 0)

  val r = Ref(0)
  Future {
    atomic { implicit txn =>
      Thread.sleep(250)
      r() = 1
    }
  }
  val (t2, i2) = atomicRollbackCount { implicit txn =>
    val _r = r()
    Thread.sleep(500)
    _r + r()
  }

  assert(t2 == 2)
  assert(i2 == 1)
}