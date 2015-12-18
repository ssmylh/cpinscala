package chapter2

import Exercise._

object App extends App {
  // 1
  val (a, b) = parallel({
    Thread.sleep(1 * 1000)
    1
  }, {
    Thread.sleep(1 * 1000)
    2
  })
  assert((a, b) == (1, 2))

  // 4
  {
    val sv = new SyncVar[Int]
    val last = 15
    val producer = thread {
      for (i <- 0 until last) {
        while (sv.nonEmpty) {}
        sv.put(i)
      }
    }
    val consumer = thread {
      var i = 0
      while (i != last - 1) {
        while (sv.isEmpty) {}
        i = sv.get()
        val id = Thread.currentThread().getId
        println(s"exercise4 $id - $i")
      }
    }
  }

  // 6
  {
    val sq = new SyncQueue[Int](5)
    val last = 15
    val producer = thread {
      for (i <- 0 until last) {
        sq.putWait(i)
      }
    }
    val consumer = thread {
      var i = 0
      while (i != last - 1) {
        i = sq.getWait()
        val id = Thread.currentThread().getId
        println(s"exercise6 $id - $i")
      }
    }
  }

  // 8
  {
    PriorityTaskPool.start()
    for (i <- 0 until 5) {
      PriorityTaskPool.asynchronous(i) {
        val id = Thread.currentThread().getId
        println(s"exercise8 $id - $i")
        Thread.sleep(100)
      }
    }
    Thread.sleep(1000)
    PriorityTaskPool.shatdown()
  }

  // 9
  {
    val pool = new No9.PriotiryTaskPool(4)
    pool.start()
    for (i <- 0 until 20) {
      pool.asynchronous(i) {
        val id = Thread.currentThread().getId
        println(s"exercise9 $id - $i")
        Thread.sleep(100)
      }
    }
    Thread.sleep(1000)
    pool.shatdown()
  }
}