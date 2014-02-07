package models

import reactivemongo.bson._

case class AccumulatedBuildScore(_id: Int, scoreOfBuild: Int)

object AccumulatedBuildScore {
  implicit val accumulatedBuildScoreHandler = Macros.handler[AccumulatedBuildScore]
}
//
//object AccumulatedBuildScore {
//  implicit object AccumulatedBuildScoreBSONReader extends BSONDocumentReader[AccumulatedBuildScore] {
//    def read(score: BSONDocument): AccumulatedBuildScore = {
//      AccumulatedBuildScore(
//        score.getAs[Int]("_id").get,
//        score.getAs[Int]("scoreOfBuild").get)
//    }
//  }
//  implicit object AccumulatedBuildScoreBSONWriter extends BSONDocumentWriter[AccumulatedBuildScore] {
//    def write(score: AccumulatedBuildScore): BSONDocument = {
//      BSONDocument(
//        "_id" -> score.build,
//        "score" -> score.score)
//    }
//  }
//}
