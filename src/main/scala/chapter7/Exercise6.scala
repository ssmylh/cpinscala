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

    val nts = ts.filterNot(_ == ts(n))
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
  val buf1 = new TArrayBuffer[Int]()
  buf1 += 1
  buf1 += 2
  assert(buf1.toList == List(1, 2))

  val buf2 = new TArrayBuffer[Int]()
  1 +=: buf2
  0 +=: buf2
  assert(buf2.toList == List(0, 1))
  assert(buf2(0) == 0)

  val buf3 = new TArrayBuffer[Int]()
  buf3 += 1
  buf3 += 2
  buf3.clear()
  assert(buf3.toList == Nil)

  val buf4 = new TArrayBuffer[Int]()
  buf4 += 1
  buf4 += 5
  buf4.insertAll(1, List(2, 3, 4))
  assert(buf4.toList == List(1, 2, 3, 4, 5))

  val buf5 = new TArrayBuffer[Int]()
  buf5 += 1
  buf5 += 2
  buf5 += 3
  buf5.remove(1)
  assert(buf5.toList == List(1, 3))

  val buf6 = new TArrayBuffer[Int]()
  buf6 += 1
  buf6 += 2
  buf6(1) = 3
  assert(buf6.toList == List(1, 3))
}