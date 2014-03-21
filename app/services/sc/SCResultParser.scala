package services.sc

import com.codecommit.antixml._
import models.TestCaseKey

case class TestRun(id: Int, name: String)

object SCResultParser {

  def parseExecutedConfigs(xmlStr: String) = {
    val xml = XML.fromString(xmlStr)
    val rows = xml \\ "row"
    rows.map { row =>
      val columns = (row \ "column")
      val runId = columns.filter(_.attr("name") == Some("ExecDefRunID_pk_fk")).head.children.toString.toInt
      val cfgName = columns.filter(_.attr("name") == Some("ExecServerName")).head.children.toString
      TestRun(runId, cfgName)
    }
  }

  def parseResultsForRun(xmlStr: String) = {
    val xml = XML.fromString(xmlStr)
    val rows = xml \\ "row"
    val mapped = rows.map { row =>
      val columns = (row \ "column")
      val suiteName = columns.filter(_.attr("name") == Some("Suite")).head \ text mkString
      val className = columns.filter(_.attr("name") == Some("ExternalID_pk")).head \ text mkString
      val testName = columns.filter(_.attr("name") == Some("TestDefName")).head \ text mkString
      
      TestCaseKey(suiteName, className, testName)
    }
    mapped.toList
  }

}