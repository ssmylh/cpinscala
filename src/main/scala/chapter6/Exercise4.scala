package chapter6

import rx.lang.scala._

class Signal[T] {
  protected var lastEvent: Option[T] = None
  protected var observable: Observable[T] = _

  def this(observable: Observable[T]) {
    this()
    setObservable(observable)
  }

  def this(observable: Observable[T], initial: T) {
    this(observable)
    lastEvent = Option(initial)
  }

  protected def setObservable(observable: Observable[T]): Unit = {
    this.observable = observable
    this.observable.subscribe(t => { lastEvent = Option(t) })
  }

  /* This method throws `NoSuchElementException` when any of events are not emitted. */
  def apply(): T = lastEvent.get

  def map[S](f: T => S): Signal[S] = lastEvent match {
    case Some(t) => new Signal(observable.map(f), f(t))
    case _ => new Signal(observable.map(f))
  }

  def zip[S](that: Signal[S]): Signal[(T, S)] = (lastEvent, that.lastEvent) match {
    case (Some(t), Some(s)) => new Signal(observable.zip(that.observable), (t, s))
    case (_, _) => new Signal(observable.zip(that.observable))
  }

  def scan[S](z: S)(f: (S, T) => S): Signal[S] = lastEvent match {
    case Some(t) => {
      val s = f(z, t)
      new Signal(observable.scan(s)(f), s)
    }
    case _ => new Signal(observable.scan(z)(f))
  }
}

object Exercise4 extends App {
  implicit class ToSignal[T](val self: Observable[T]) extends AnyVal {
    def toSignal: Signal[T] = new Signal(self)
  }

  val o1 = Subject[Int]()
  val s1 = o1.toSignal
  o1.onNext(1)
  assert(s1() == 1)

  val o2 = Subject[Int]()
  val s2 = o2.toSignal
  o2.onNext(1)
  val increment = s2.map(_ + 1)
  assert(increment() == 2)
  o2.onNext(2)
  assert(increment() == 3)

  val o31 = Subject[Int]()
  val o32 = Subject[String]()
  val s31 = o31.toSignal
  val s32 = o32.toSignal
  o31.onNext(1)
  o32.onNext("a")
  val zipped = s31.zip(s32)
  assert(zipped() == (1, "a"))
  o31.onNext(2)
  o32.onNext("b")
  assert(zipped() == (2, "b"))

  val o4 = Subject[Int]()
  val s4 = o4.toSignal
  o4.onNext(1)
  val sum = s4.scan(0)(_ + _)
  o4.onNext(2)
  assert(sum() == 3)
  o4.onNext(3)
  assert(sum() == 6)
}