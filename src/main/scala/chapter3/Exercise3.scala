package chapter3

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

import scala.concurrent._
import scala.sys.process._

object Exercise3 {
  // 1
  class PiggybackContext extends ExecutionContext {
    def execute(runnable: Runnable): Unit = {
      try {
        runnable.run()
      } catch {
        case t: Throwable => reportFailure(t)
      }
    }

    def reportFailure(cause: Throwable): Unit = {
      cause.printStackTrace()
    }
  }

  // 2
  class TreiberStack[T] {
    private val stack = new AtomicReference[List[T]](Nil)

    @annotation.tailrec
    final def push(x: T): Unit = {
      val xs = stack.get
      // can use lock-free ( since List is immutable ).
      if (!stack.compareAndSet(xs, x :: xs))
        push(x)
    }

    @annotation.tailrec
    final def pop(): T = {
      val xs = stack.get
      xs match {
        case Nil => throw new RuntimeException("Stack is empty.")
        case h :: t =>
          if (stack.compareAndSet(xs, t))
            h
          else
            pop()
      }
    }
  }

  // 3
  class ConcurrentSortedList[T](implicit val ord: Ordering[T]) {
    case class Elements(head: T, tail: AtomicReference[Option[Elements]])

    private val elements: AtomicReference[Option[Elements]] = new AtomicReference(None)
    def add(x: T): Unit = {
      _add(x, elements)
    }

    @annotation.tailrec
    private def _add(head: T, tail: AtomicReference[Option[Elements]]): Unit = {
      val opt = tail.get
      opt match {
        case None => {
          val elms = Elements(head, new AtomicReference(None))
          if (!tail.compareAndSet(opt, Some(elms)))
            _add(head, tail)
        }
        case Some(e) => {
          if (ord.compare(head, e.head) > 0) {
            _add(head, e.tail)
          } else {
            val elms = Elements(head, new AtomicReference(opt))
            if (!tail.compareAndSet(opt, Some(elms)))
              _add(head, tail)
          }
        }
      }
    }

    def iterator: Iterator[T] = new Iterator[T] {
      var opt = elements.get
      def hasNext: Boolean = opt.nonEmpty
      def next(): T = {
        opt match {
          case None => throw new NoSuchElementException
          case Some(e) => {
            opt = e.tail.get
            e.head
          }
        }
      }
    }
  }

  // 5
  class LazyCell[T](initialization: => T) {
    @volatile
    private var initialized: Boolean = false
    private var value: T = _
    private val lock = new AnyRef
    def apply(): T = {
      // use double-checked-locking
      if (initialized)
        value
      else {
        lock.synchronized {
          if (!initialized) {
            value = initialization
            initialized = true
          }
          value
        }
      }
    }
  }

  // 6
  class PureLazyCell[T](initialization: => T) {
    private val ref = new AtomicReference[Option[T]](None)
    @annotation.tailrec
    final def apply(): T = {
      val value = ref.get
      value match {
        case Some(t) => t
        case None => {
          val t = initialization
          if (ref.compareAndSet(value, Some(t)))
            t
          else
            apply()
        }
      }
    }
  }

  // 7
  class SyncConcurrentMap[K, V] extends collection.concurrent.Map[K, V] {
    private val map = collection.mutable.Map.empty[K, V]
    private val lock = new AnyRef
    def +=(kv: (K, V)): SyncConcurrentMap.this.type = lock.synchronized {
      map += kv
      this
    }

    def -=(key: K): SyncConcurrentMap.this.type = lock.synchronized {
      map -= key
      this
    }

    def get(key: K): Option[V] = lock.synchronized {
      map.get(key)
    }

    def iterator: Iterator[(K, V)] = lock.synchronized {
      map.iterator
    }

    def putIfAbsent(k: K, v: V): Option[V] = lock.synchronized {
      map.get(k) match {
        case None => {
          map(k) = v
          None
        }
        case old @ Some(_) => old
      }
    }

    def remove(k: K, v: V): Boolean = lock.synchronized {
      map.get(k) match {
        case Some(stored) if stored == v => {
          map.remove(k)
          true
        }
        case _ => false
      }
    }

    def replace(k: K, v: V): Option[V] = lock.synchronized {
      map.get(k) match {
        case None => None
        case old @ Some(_) => {
          map(k) = v
          old
        }
      }
    }

    def replace(k: K, oldvalue: V, newvalue: V): Boolean = lock.synchronized {
      map.get(k) match {
        case Some(v) if (v == oldvalue) => {
          map(k) = v
          true
        }
        case _ => false
      }
    }
  }

  // 8
  // This method's preconditions are the following:
  //   - the `scala` command is added to the `PATH` variable.
  //   - In case of executing in sbt, set `fork` setting to `true` (set fork := true ).
  def spawn[T](block: => T): T = {
    val className = EvaluationServer.getClass().getName().split((Pattern.quote("$")))(0)
    val lines = Process(s"scala -cp ${System.getProperty("java.class.path")} $className").lineStream
    // wait for outputting port
    val port = lines.head.toInt

    val socket = new Socket("127.0.0.1", port)
    try {
      val out = new ObjectOutputStream(socket.getOutputStream())
      out.writeObject(() => block) // wrap `block` not to be evaluated.
      val in = new ObjectInputStream(socket.getInputStream())
      in.readObject() match {
        case e: Throwable => throw e
        case x => x.asInstanceOf[T]
      }
    } finally {
      socket.close()
    }
  }
}