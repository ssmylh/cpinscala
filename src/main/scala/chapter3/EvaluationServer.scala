package chapter3

import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import java.net.ServerSocket

import scala.language.reflectiveCalls
import scala.util._

object EvaluationServer extends App {

  // port argument
  val port = args(0).toInt

  // this server expects client to transfer `Function0` object.
  using(new ServerSocket(port)) { serverSocket =>
    using(serverSocket.accept()) { socket =>
      val in = new ObjectInputStream(socket.getInputStream())
      val f0 = in.readObject().asInstanceOf[Function0[Any]]

      val out = new ObjectOutputStream(socket.getOutputStream())
      Try { f0() } match {
        case Success(v) => out.writeObject(v)
        case Failure(e) => out.writeObject(e)
      }
    }
  }

  def using[A, R <: { def close() }](resource: R)(f: R => A): A =
    try {
      f(resource)
    } finally {
      resource.close()
    }
}