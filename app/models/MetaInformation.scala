package models

import org.jboss.netty.buffer._
import play.api.data._
import play.api.data.validation.Constraints._
import reactivemongo.bson._

case class MetaInformation(key: String, value: String)

object MetaInformation {
  implicit object MetaInformationBSONReader extends BSONDocumentReader[MetaInformation] {
    def read(inf: BSONDocument): MetaInformation = {
      MetaInformation(
        inf.getAs[String]("_id").get,
        inf.getAs[String]("value").get)
    }
  }
  implicit object MetaInformationBSONWriter extends BSONDocumentWriter[MetaInformation] {
    def write(inf: MetaInformation): BSONDocument = {
      BSONDocument(
        "_id" -> inf.key,
        "value" -> inf.value)
    }
  }
}