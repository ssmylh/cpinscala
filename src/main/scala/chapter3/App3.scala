package chapter3

import chapter3.Exercise3._

object App3 extends App {
  // 2
  import chapter2.Exercise2._
  val stack = new TreiberStack[Int]
  val t1 = thread {
    for (i <- 0 until 50) {
      stack.push(i)
    }
  }
  val t2 = thread {
    for (i <- 50 until 100) {
      stack.push(i)
    }
  }
  t1.join()
  t2.join()

  val buffer = scala.collection.mutable.ListBuffer.empty[Int]
  for (_ <- 0 until 100) {
    buffer += stack.pop()
  }
  assert(buffer.toList.sorted == (0 until 100).toList)
  try {
    stack.pop()
  } catch {
    case t: Throwable => assert(t.isInstanceOf[RuntimeException])
  }

  // 3
  val csl = new ConcurrentSortedList[Int]
  val t3 = thread {
    for (i <- 49 to 0 by -1) {
      csl.add(i)
    }
  }
  val t4 = thread {
    for (i <- 99 to 50 by -1) {
      csl.add(i)
    }
  }
  t3.join()
  t4.join()
  assert(csl.iterator.toList == (0 until 100).toList)

  // 5
  val lc = new LazyCell[String]({
    log("LazyCell initialization start.")
    Thread.sleep(500)
    log("LazyCell initialization end.")
    "Hello, World"
  })
  val t5 = thread {
    log(lc.apply())
  }
  val t6 = thread {
    log(lc.apply())
  }

  // 7
  val scm = new SyncConcurrentMap[Symbol, Int]
  assert(scm.get('a) == None)
  scm.putIfAbsent('a, 1)
  assert(scm.get('a) == Some(1))

  assert(!scm.remove('a, 2))
  assert(scm.remove('a, 1))
  assert(scm.get('a) == None)

  scm += ('b -> 1)
  assert(scm.replace('b, 2) == Some(1))

  scm += ('c -> 1)
  assert(!scm.replace('c, 2, 3))
  assert(scm.replace('c, 1, 2))

  // 8
  val s1 = spawn({
    1 + 1
  })
  assert(s1 == 2)
  try {
    val s2 = spawn({
      "test".toInt
    })
  } catch {
    case e: NumberFormatException => // OK
    case _: Throwable => assert(false)
  }

  def log(msg: String): Unit = println(s"${Thread.currentThread.getName} - $msg")
}