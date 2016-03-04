package chapter6

import scala.concurrent.duration._
import scala.util.Random

import rx.lang.scala._

object Exercise3 extends App {
  // Now IHeartQuotes (http://www.iheartquotes.com) closes, so create a string with random length.
  Random.setSeed(1)
  def randomLengthStr(maxLength: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z')
    (1 to Random.nextInt(maxLength)).map(_ => chars(Random.nextInt(chars.length))).mkString
  }

  val randomQuote: Observable[String] = Observable.create { obs =>
    obs.onNext(randomLengthStr(100))
    obs.onCompleted()
    Subscription()
  }

  val indexedQuotes: Observable[(Long, String)] = for {
    i <- Observable.interval(0.5.seconds)
    q <- randomQuote
  } yield (i + 1, q)

  val averages = indexedQuotes.scan((0L, 0L))((acc, iq) => (iq._1, acc._2 + iq._2.length))
    .tail // except the first element((0L, 0L)).
    .map(acc => acc._2.toDouble / acc._1.toDouble)

  averages.subscribe(println(_))
  Thread.sleep(30 * 1000)
}