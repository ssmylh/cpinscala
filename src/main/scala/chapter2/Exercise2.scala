package chapter2

object Exercise2 {
  // 1
  def parallel[A, B](a: => A, b: => B): (A, B) = {
    var oa: Option[A] = None
    var ob: Option[B] = None

    val ta = thread {
      oa = Some(a)
    }
    val tb = thread {
      ob = Some(b)
    }

    ta.join()
    tb.join()

    (oa, ob) match {
      case (Some(a), Some(b)) => (a, b)
      case _ => throw new IllegalStateException()
    }
  }

  // 2
  def periodically(duration: Long)(f: () => Unit): Unit = {
    thread {
      while (true) {
        f()
        Thread.sleep(duration)
      }
    }
  }

  // 3
  class SyncVar[T] {
    private var o: Option[T] = None
    def get(): T = synchronized {
      o match {
        case Some(t) =>
          o = None
          t
        case _ => throw new Exception("is empty")
      }
    }
    def put(x: T): Unit = synchronized {
      o match {
        case None => o = Some(x)
        case _ => throw new Exception("is not empty")
      }
    }
    // 4
    def isEmpty: Boolean = synchronized {
      o.isEmpty
    }
    def nonEmpty: Boolean = synchronized {
      o.nonEmpty
    }
    // 5
    def getWait(): T = synchronized {
      while (o.isEmpty) {
        wait()
      }
      notifyAll()
      o.get
    }
    def putWait(x: T): Unit = synchronized {
      while (o.nonEmpty) {
        wait()
      }
      o = Some(x)
      notifyAll()
    }
  }

  // 6
  class SyncQueue[T](n: Int) {
    import scala.collection.mutable.Queue
    private val queue = Queue[T]()

    def isEmpty: Boolean = synchronized {
      queue.isEmpty
    }
    def nonEmpty: Boolean = synchronized {
      queue.nonEmpty
    }
    def getWait(): T = synchronized {
      while (queue.isEmpty) {
        wait()
      }
      notifyAll()
      queue.dequeue()
    }
    def putWait(x: T): Unit = synchronized {
      while (queue.size == n) {
        wait()
      }
      queue.enqueue(x)
      notifyAll()
    }
  }

  // 8
  object PriorityTaskPool {
    import scala.collection.mutable.PriorityQueue
    implicit val taskOrdering: Ordering[(Int, () => Unit)] = Ordering.by(_._1)
    private val tasks = PriorityQueue[(Int, () => Unit)]()

    private var terminated = false
    object Worker extends Thread {
      def poll(): Option[(Int, () => Unit)] = tasks.synchronized {
        while (tasks.isEmpty && !terminated) {
          tasks.wait()
        }
        if (terminated)
          None
        else
          Some(tasks.dequeue())
      }
      @scala.annotation.tailrec
      override def run() = poll() match {
        case Some(task) =>
          task._2()
          run()
        case _ =>
      }
    }
    def start(): Unit = Worker.start()
    def shatdown(): Unit = tasks.synchronized {
      terminated = true;
      tasks.notifyAll()
    }
    def asynchronous(priority: Int)(body: => Unit): Unit = tasks.synchronized {
      tasks.enqueue((priority, () => body))
      tasks.notifyAll()
    }
  }

  object No9 {
    class PriotiryTaskPool(p: Int) {
      import scala.collection.mutable.PriorityQueue
      import scala.collection.mutable.ListBuffer
      implicit val taskOrdering: Ordering[(Int, () => Unit)] = Ordering.by(_._1)
      private val tasks = PriorityQueue[(Int, () => Unit)]()
      private val lock = new AnyRef
      private var terminated = false

      private val workers = ListBuffer[Worker]()
      class Worker extends Thread {
        def poll(): Option[(Int, () => Unit)] = lock.synchronized {
          while (tasks.isEmpty && !terminated) {
            lock.wait()
          }
          if (terminated)
            None
          else
            Some(tasks.dequeue())
        }
        @scala.annotation.tailrec
        final override def run() = poll() match {
          case Some(task) =>
            task._2()
            run()
          case _ =>
        }
      }

      def start(): Unit = {
        for (i <- 0 until p) {
          val worker = new Worker
          workers += worker
          worker.start()
        }
      }
      def shatdown(): Unit = lock.synchronized {
        terminated = true
        lock.notifyAll()
      }
      def asynchronous(priority: Int)(body: => Unit): Unit = lock.synchronized {
        tasks.enqueue((priority, () => body))
        lock.notifyAll()
      }
    }
  }

  def thread(body: => Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
}