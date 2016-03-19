package chapter6

import java.io._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import rx.lang.scala._

object Exercise8 extends App {

  def copyFile(src: String, dest: String): Observable[Double] = {
    val dummy = Observable.create[Double] { obs =>
      obs.onNext(0)
      for (i <- 1 to 100) {
        Thread.sleep(50)
        obs.onNext(i)
      }
      obs.onNext(101d)
      obs.onCompleted()
      Subscription()
    }

    def copy(src: String, dest: String): Observable[Double] = Observable.create { obs =>
      obs.onNext(0d)

      val file = new File(src)
      val size = file.length()
      val in = new BufferedInputStream(new FileInputStream(file))
      val out = new BufferedOutputStream(new FileOutputStream(dest))
      try {
        var copiedSize: Long = 0l
        val buffer = new Array[Byte](1024)
        @annotation.tailrec
        def go(): Unit = {
          val readBytes = in.read(buffer)
          if (readBytes != -1) {
            out.write(buffer, 0, readBytes)
            copiedSize += readBytes
            if (copiedSize == size) {
              obs.onNext(100d)
            } else {
              obs.onNext(copiedSize.toDouble / size.toDouble)
            }
            go()
          }
        }

        go()
        obs.onNext(101d)
        obs.onCompleted()
      } catch {
        case e: Exception => obs.onError(e)
      } finally {
        in.close()
        out.close()
      }
      Subscription()
    }

    Observable.interval(0.1.seconds).combineLatest(dummy).takeWhile(_._2 <= 100).map(_._2)
  }

  def log(msg: String): Unit = println(s"${Thread.currentThread.getName} - $msg")
  val o = copyFile("", "").subscribe(x => log(s"$x %"))
  Thread.sleep(10 * 1000)
}