package chapter7

import scala.concurrent.stm._

class TPair[P, Q](pinit: P, qinit: Q) {
  private[this] val p: Ref[P] = Ref(pinit)
  private[this] val q: Ref[Q] = Ref(qinit)

  def first(implicit txn: InTxn): P = p.get
  def first_=(x: P)(implicit txn: InTxn): Unit = p.set(x)

  def second(implicit txn: InTxn): Q = q.get
  def second_=(x: Q)(implicit txn: InTxn): Unit = q.set(x)

  def swap()(implicit e: P =:= Q, txn: InTxn): Unit = {
    val _p = first
    first = second.asInstanceOf[P]
    second = _p
  }
}

object Exercise1 extends App {
  val p1 = new TPair(1, 2)
  atomic { implicit txn =>
    assert(p1.first == 1)
    p1.first = 3
    assert(p1.first == 3)

    assert(p1.second == 2)
    p1.second = 4
    assert(p1.second == 4)

    p1.swap()
    assert(p1.first == 4)
    assert(p1.second == 3)
  }
}