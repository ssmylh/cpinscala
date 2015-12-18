package chapter1

object Exercise {
  // 1
  def compose[A, B, C](g: B => C, f: A => B): A => C = a => g(f(a))

  // 2
  def fuse[A, B](oa: Option[A], ob: Option[B]): Option[(A, B)] = for {
    a <- oa
    b <- ob
  } yield (a, b)

  // 3
  def check[T](xs: Seq[T])(pred: T => Boolean): Boolean = xs.forall { x =>
    try {
      pred(x)
    } catch {
      case _: Exception => false
    }
  }

  // 4
  def permutations(s: String): Seq[String] = {
    if (s.length == 0) Seq("")
    else for {
      i <- 0 until s.length
      x <- permutations(s.take(i) + s.takeRight(s.length - i - 1))
    } yield s(i) + x
  }
}