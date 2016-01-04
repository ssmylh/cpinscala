package chapter5

import org.scalameter._

object Exercise1 extends App {
  // The `org.scalameter` package object extends MeasureBuilder,
  // then assigns `MeasureBuilder.average` to `MeasureBuilder.resultFunction`.
  val times = 1000000
  val avgMsec = config(
    Key.exec.benchRuns -> times
  ) withWarmer {
    new Warmer.Default
  } withMeasurer {
    new Measurer.IgnoringGC
  } measure {
    val obj = new Object
  }
  println(s"average ${avgMsec.value * 1000000.0} nanoseconds")
}