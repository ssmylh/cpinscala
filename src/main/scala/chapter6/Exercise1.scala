package chapter6

import scala.collection.JavaConverters._
import scala.concurrent.duration._

import rx.lang.scala._

object Exercise1 extends App {

  @volatile var threads = Thread.getAllStackTraces().keySet().asScala.toSet
  def detectedThreads: Observable[Thread] = Observable.create { obs =>
    val currentThreads = Thread.getAllStackTraces().keySet().asScala.toSet
    val newThreads = currentThreads.filter(!threads.contains(_))
    threads = currentThreads

    newThreads.foreach(obs.onNext(_))
    Subscription()
  }

  val obs: Observable[Thread] = for {
    _ <- Observable.interval(0.5.seconds)
    t <- detectedThreads
  } yield t
  obs.subscribe(t => log(t.toString))

  def startThread(name: String): Unit = {
    val thread = new Thread(name) {
      override def run(): Unit = {
        Thread.sleep(1500)
      }
    }
    thread.start()
  }

  Thread.sleep(1000)
  startThread("thread-1")
  Thread.sleep(1000)
  startThread("thread-2")
  Thread.sleep(2000)

  def log(msg: String): Unit = println(s"${Thread.currentThread.getName} - $msg")
}