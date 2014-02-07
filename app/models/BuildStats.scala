package models

import reactivemongo.bson._
import java.text.DecimalFormat

case class BuildStats(_id: Int, scoreOfBuild: Int) {
  def scoreFormatted = {
    val postfixes = List("k", "m", "b")
    val digitGroups = (Math.log10(scoreOfBuild) / Math.log10(1000)).toInt
      s"${new DecimalFormat("#,##0.#").format(scoreOfBuild / Math.pow(1000, digitGroups))}${postfixes(digitGroups - 1)}"
  }
}

object BuildStats {
  implicit val buildStatsHandler = Macros.handler[BuildStats]
}
//  }
//  implicit object AccumulatedBuildScoreBSONWriter extends BSONDocumentWriter[AccumulatedBuildScore] {
//    def write(score: AccumulatedBuildScore): BSONDocument = {
//      BSONDocument(
//        "_id" -> score.build,
//        "score" -> score.score)
//    }
//  }
//}
