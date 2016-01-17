package chapter5

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.parallel.mutable.ParArray

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object Exercise4 extends JFXApp {
  /* The game of life - Die Hard (after 130 generations, all cells are dead) */
  val offset = (20, 20)
  val initialDieHardIndexes = Map(
    1 -> Map(2 -> true),
    2 -> Map(2 -> true, 3 -> true),
    6 -> Map(3 -> true),
    7 -> Map(1 -> true, 3 -> true),
    8 -> Map(3 -> true)).map {
      case (x, ys) =>
        (x + offset._1, ys.map {
          case (y, b) =>
            (y + offset._2, b)
        })
    }

  val maxX = 50
  val maxY = 50
  val width = 500
  val height = 500
  val rectangleWidth = width / maxX
  val rectangleHeight = height / maxY

  @volatile var cells =
    ParArray.tabulate[Boolean](maxX, maxY)((x, y) => initialDieHardIndexes.get(x).flatMap(_.get(y)).isDefined)

  @volatile var rectangles: Array[Array[Rectangle]] = Array.ofDim[Rectangle](maxX, maxY)
  for {
    _x <- 0 until maxX
    _y <- 0 until maxY
  } rectangles(_x)(_y) = new Rectangle {
    x = _x * rectangleWidth
    y = _y * rectangleHeight
    width = rectangleWidth
    height = rectangleHeight
    fill = if (cells(_x)(_y)) Color.Black else Color.White
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "The Gema of life - Die Hard"
    scene = new Scene {
      content = rectangles.flatten
    }
  }
  stage.setWidth(width)
  stage.setHeight(height)

  val generation = new AtomicInteger(0)
  val task = new TimerTask {
    def run(): Unit = {
      cells = nextGenerationCells(cells, maxX, maxY)
      var liveCellsNum = 0
      for {
        x <- 0 until maxX
        y <- 0 until maxY
      } {
        val alive = cells(x)(y)
        rectangles(x)(y).setFill(if (alive) Color.Black else Color.White)
        if (alive) liveCellsNum += 1
      }

      generation.incrementAndGet()
      println(s"after ${generation.get()} generation, live cells : $liveCellsNum")

      if (generation.get() == 130) {
        this.cancel()
        timer.cancel()
      }
    }
  }
  val timer = new Timer
  timer.schedule(task, 0, 1 * 250)

  def nextGenerationCells(cells: ParArray[ParArray[Boolean]], maxX: Int, maxY: Int): ParArray[ParArray[Boolean]] =
    cells.zipWithIndex.map {
      case (ys, x) => ys.zipWithIndex.map {
        case (state, y) => {
          val liveNeighboursNum = neighbours(cells, maxX, maxY, x, y).count { case (_, _, s) => s }
          val nextState =
            if (state) {
              if (liveNeighboursNum == 2 || liveNeighboursNum == 3) true
              else false
            } else {
              if (liveNeighboursNum == 3) true
              else false
            }
          nextState
        }
      }
    }

  def neighbours(cells: ParArray[ParArray[Boolean]], maxX: Int, maxY: Int, x: Int, y: Int): Seq[(Int, Int, Boolean)] = for {
    ix <- x - 1 to x + 1 if ix > 0 && ix < maxX
    iy <- y - 1 to y + 1 if iy > 0 && iy < maxY
    if (ix != x || iy != y)
  } yield (ix, iy, cells(ix)(iy))

}