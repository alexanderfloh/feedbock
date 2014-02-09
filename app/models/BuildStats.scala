package models

import reactivemongo.bson._
import java.text.DecimalFormat
import reactivemongo.bson.Macros.Annotations.Key

case class BuildStats(@Key("_id") build: Int, scoreOfBuild: Int) {
  def scoreFormatted = {
    val postfixes = List("k", "m", "b")
    val digitGroups = (Math.log10(scoreOfBuild) / Math.log10(1000)).toInt
    val numberFormatted = new DecimalFormat("#,##0.#").format(scoreOfBuild / Math.pow(1000, digitGroups))
    s"$numberFormatted${postfixes(digitGroups - 1)}"
  }
}

object BuildStats {
  implicit val handler = Macros.handler[BuildStats]
}
