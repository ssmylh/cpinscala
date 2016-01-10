package chapter5

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint._
import scalafx.scene.shape.Rectangle

object Exercise3 extends JFXApp {
  val width = 500
  val height = 500

  val maxR = 2d
  val minR = -2d
  val maxI = 2d
  val minI = -2d

  val unitX = (maxR - minR) / width
  val unitY = (maxI - minI) / height

  case class Pixel(x: Int, y: Int, mandelbrot: Boolean)
  def createPixel(x: Int, y: Int): Pixel = {
    val r = (x - width / 2) * unitX
    val i = -(y - height / 2) * unitY
    Pixel(x, y, elementOfMandelbrotSet(r, i))
  }

  def elementOfMandelbrotSet(r: Double, i: Double): Boolean = {
    @annotation.tailrec
    def go(zr: Double, zi: Double, cr: Double, ci: Double, iteration: Int): Boolean = {
      val maxIteration = 500
      if (zr * zr + zi * zi > 4) false
      else if (iteration > maxIteration) true
      else {
        val nextX = zr * zr - zi * zi + cr
        val nextY = 2 * zr * zi + ci
        go(nextX, nextY, cr, ci, iteration + 1)
      }
    }
    go(0, 0, r, i, 0)
  }

  val pixels = (for {
    x <- 0 to width
    y <- 0 to height
  } yield (x, y)).par.map {
    case (x, y) => createPixel(x, y)
  }

  val rectangles = pixels.map(p => new Rectangle {
    x = p.x
    y = p.y
    width = 1
    height = 1
    fill = if (p.mandelbrot) Color.Blue else Color.Aqua
  })

  stage = new JFXApp.PrimaryStage {
    title.value = "Mandelbrot Set"
    scene = new Scene {
      content = rectangles.seq
    }
  }
  stage.setWidth(width)
  stage.setHeight(height)
}