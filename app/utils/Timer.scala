package utils

import play.Logger

object Timer {
  def apply[T](name: String)(r: => T): T = {
    val start = System.currentTimeMillis
    val ret:T = r
    val end = System.currentTimeMillis
    Logger.info(s"Timer '$name': ${end - start}ms")
    ret
  }
}