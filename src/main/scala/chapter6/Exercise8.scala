package chapter6

import java.io._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import rx.lang.scala._

object Exercise8 extends App {

  def log(msg: String): Unit = println(s"${Thread.currentThread.getName} - $msg")

  val copy = Observable.create[Long] { obs =>
    obs.onNext(0)
    for (i <- 1 to 10) {
      Thread.sleep(500)
//      if (i == 6) {
//        obs.onError(new Exception("test"))
//      }
      obs.onNext(i)
    }
    obs.onCompleted()
    Subscription()
  }

  //val o = copy.subscribeOn(schedulers.ComputationScheduler())
  //val o = copy.subscribeOn(schedulers.ComputationScheduler())
  val interval = Observable.interval(0.1.seconds)

  interval.combineLatest(copy).takeWhile(x => x._2 < 10).subscribe(x => log(s"$x"), e => e.printStackTrace())

//  val sig = new Signal(copy.observeOn(schedulers.ComputationScheduler()))
//  println(1)
//  println(sig())

  Thread.sleep(10 * 1000)

  //  def copyFile(src: String, dest: String): Observable[Double] = {
  //
  //    def withProgress(src: String, dest: String): Observable[Double] = Observable.create { obs =>
  //      val file = new File(src)
  //      val size = file.length()
  //
  //      val in = new BufferedInputStream(new FileInputStream(file))
  //      val out = new BufferedOutputStream(new FileOutputStream(dest))
  //      try {
  //        var progressSize = 0
  //
  //        val buffer = new Array[Byte](8192)
  //        def go(): Unit = {
  //          val readBytes = in.read(buffer)
  //          if (readBytes != -1) {
  //            out.write(buffer, 0, readBytes)
  //            progressSize += readBytes
  //            obs.onNext(progressSize.toDouble / size.toDouble)
  //            go()
  //          }
  //        }
  //        go()
  //        obs.onCompleted()
  //      } catch {
  //        case e: IOException => obs.onError(e)
  //      } finally {
  //        in.close()
  //        out.close()
  //      }
  //
  //      Subscription()
  //    }
  //
  //    for {
  //      _ <- Observable.interval(0.1.seconds)
  //      p <- withProgress(src, dest)
  //    } yield p
  //  }

}