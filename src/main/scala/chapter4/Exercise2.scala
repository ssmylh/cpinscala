package chapter4

import scala.concurrent._

class IVar[T] {
  private val promise = Promise[T]
  def apply(): T = {
    if (!promise.isCompleted) {
      throw new IllegalStateException("value is not assigned.")
    }
    Await.result(promise.future, duration.Duration.Inf)
  }
  def :=(x: T): Unit = {
    if (!promise.trySuccess(x)) {
      throw new IllegalStateException("value is already assigned.")
    }
  }
}

object Exercise2 extends App {
  val iv1 = new IVar[Int]

  try {
    iv1.apply()
    assert(false)
  } catch {
    case e: IllegalStateException =>
  }

  iv1 := 1
  try {
    iv1 := 1
    assert(false)
  } catch {
    case e: IllegalStateException =>
  }

  assert(iv1.apply() == 1)
}