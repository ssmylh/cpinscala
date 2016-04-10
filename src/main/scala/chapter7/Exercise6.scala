package chapter7

import scala.concurrent.stm._

class TArrayBuffer[T] extends scala.collection.mutable.Buffer[T] {
  private[this] val buf: Ref[Vector[T]] = Ref(Vector.empty[T]) // should store the immutable object in order to use CAS operations.

  @annotation.tailrec
  final def +=(elem: T): this.type = {
    val ts = buf.single()
    if (buf.single.compareAndSet(ts, ts :+ elem))
      this
    else
      +=(elem)
  }

  @annotation.tailrec
  final def +=:(elem: T): this.type = {
    val ts = buf.single()
    if (buf.single.compareAndSet(ts, elem +: ts))
      this
    else
      +=:(elem)
  }

  def apply(n: Int): T = buf.single()(n)

  @annotation.tailrec
  final def clear(): Unit = {
    val ts = buf.single()
    if (!buf.single.compareAndSet(ts, Vector.empty[T]))
      clear()
  }

  @annotation.tailrec
  final def insertAll(n: Int, elems: collection.Traversable[T]): Unit = {
    val ts = buf.single()
    val len = ts.length
    if (len < 0 || n > len) throw new IndexOutOfBoundsException(n.toString)

    val left = ts.take(n)
    val right = ts.drop(n)
    val nts = left ++ elems ++ right
    if (!buf.single.compareAndSet(ts, nts))
      insertAll(n, elems)
  }

  def length: Int = buf.single().length

  @annotation.tailrec
  final def remove(n: Int): T = {
    val ts = buf.single()
    val len = ts.length
    if (len < 0 || n > len) throw new IndexOutOfBoundsException(n.toString)

    val left = ts.take(n)
    val right = ts.drop(n + 1)
    val nts = left ++ right
    if (buf.single.compareAndSet(ts, nts))
      ts(n)
    else
      remove(n)
  }

  @annotation.tailrec
  final def update(n: Int, newelem: T): Unit = {
    val ts = buf.single()
    val len = ts.length
    if (len < 0 || n > len) throw new IndexOutOfBoundsException(n.toString)

    val nts = ts.updated(n, newelem)
    if (!buf.single.compareAndSet(ts, nts))
      update(n, newelem)
  }

  def iterator: Iterator[T] = buf.single().iterator
}

object Exercise6 extends App {
  {
    val buf = new TArrayBuffer[Int]()
    buf += 1
    buf += 2
    assert(buf.toList == List(1, 2))
  }

  {
    val buf = new TArrayBuffer[Int]()
    1 +=: buf
    0 +=: buf
    assert(buf.toList == List(0, 1))
    assert(buf(0) == 0)
  }

  {
    val buf = new TArrayBuffer[Int]()
    buf += 1
    buf += 2
    buf.clear()
    assert(buf.toList == Nil)
  }

  {
    val buf = new TArrayBuffer[Int]()
    buf += 1
    buf += 5
    buf.insertAll(1, List(2, 3, 4))
    assert(buf.toList == List(1, 2, 3, 4, 5))
    assert(buf.length == 5)
  }

  {
    val buf = new TArrayBuffer[Int]()
    buf += 1
    buf += 2
    buf += 1
    buf += 3
    val removed = buf.remove(2)
    assert(removed == 1)
    assert(buf.toList == List(1, 2, 3))
  }

  {
    val buf = new TArrayBuffer[Int]()
    buf += 1
    buf += 2
    buf(1) = 3
    assert(buf.toList == List(1, 3))
  }
}