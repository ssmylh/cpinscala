package chapter4

import scala.concurrent._

// Single-assigned Map:
// Pairs of keys and values can be added to the IMap object, but they can never be removed or modified.
// A specific key can be assigned only once, and subsequent calls to update with that key results in an exception.
// Calling apply with a specific key returns a future, which is completed after that key is inserted into the map.
// In addition to futures and promises, you may use the scala.collection.concurrent.Map class.
class IMap[K, V] {
  private val map = new scala.collection.concurrent.TrieMap[K, Promise[V]]

  def update(k: K, v: V): Unit = {
    val p = Promise[V]
    p.success(v)
    map.putIfAbsent(k, p) match {
      case Some(existing) =>
        if (existing.trySuccess(v)) {
          // update success.
        } else {
          throw new IllegalStateException("can not update the existing value.")
        }
      case None =>
    }
  }

  def apply(k: K): Future[V] = {
    val p = Promise[V]
    map.putIfAbsent(k, p) match {
      case Some(existing) => existing.future
      case None => p.future
    }
  }
}

object Exercise7 extends App {
  import scala.concurrent.duration.Duration

  // case of single-thread
  {
    val map = new IMap[Symbol, Int]
    map('a) = 1
    try {
      map('a) = 2
      assert(false)
    } catch {
      case e: IllegalStateException =>
    }

    val f = map('b)
    map('b) = 1
    assert(Await.result(f, Duration.Inf) == 1)
  }

  // case of multiple-threads
  {
    import chapter2.Exercise._
    val map = new IMap[Symbol, Int]
    val totalThreads = 10

    val counter = new java.util.concurrent.atomic.AtomicInteger
    val updaters = for (_ <- 0 until totalThreads) yield thread {
      try {
        map('a) = 1
        counter.incrementAndGet()
      } catch {
        case e: IllegalStateException =>
      }
    }

    val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
    val appliers = for (_ <- 0 until totalThreads) yield thread {
      val v = Await.result(map('a), Duration.Inf)
      buf.synchronized {
        buf += v
      }
    }

    updaters.foreach(_.join())
    assert(counter.get() == 1)

    appliers.foreach(_.join())
    assert(buf.count(_ == 1) == totalThreads)
  }
}