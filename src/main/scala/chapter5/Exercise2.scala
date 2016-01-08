package chapter5

import java.util.concurrent.atomic.AtomicInteger

import scala.util.Random

import org.scalameter.api._
import org.scalameter.picklers.Implicits._

object Exercise2 extends Bench[Double] {

  Random.setSeed(1)
  def randomChar(percent: Int): Char = {
    val chars = ('a' to 'z') ++ ('A' to 'Z')
    if (Random.nextDouble() > (percent.toDouble / 100)) chars(Random.nextInt(chars.length))
    else ' '
  }
  def randomString(length: Int, percent: Int): String =
    (0 until length).map(_ => randomChar(percent)).mkString

  lazy val executor: Executor[Double] = LocalExecutor(
    new Executor.Warmer.Default,
    Aggregator.min,
    measurer)
  lazy val measurer: Measurer[Double] = new Measurer.IgnoringGC
  lazy val persistor: Persistor = Persistor.None
  lazy val reporter: Reporter[Double] = ChartReporter(ChartFactory.XYLine())

  val percent = Gen.range("percent")(0, 100, 1)
  val length = 30000
  val randomStrings = percent.map(randomString(length, _))

  performance of "foreach" in {
    measure method "sequential" in {
      using(randomStrings) in { str =>
        var counter = 0
        str.foreach(c => if (c == ' ') counter += 1)
      }
    }

    measure method "parallel" in {
      using(randomStrings) in { str =>
        var counter = new AtomicInteger(0)
        str.par.foreach(c => if (c == ' ') counter.incrementAndGet())
      }
    }
  }
}