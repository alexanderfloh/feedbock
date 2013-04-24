package results
import scala.xml.XML
import models.TestCase
import se.radley.plugin.salat.Binders._
import models.TestStatus

case class Build(number: Int, url: String)

object Results {
  
  def loadResultsForBuild(build: Build) = {
    val xml = XML.load(ResultLoader.fromUrl(build.url + "/testReport/api/xml"))
    println("parsing")
    val testcaseNodes = xml \\ "case"
    testcaseNodes.map(tc => {
      val testName = tc \ "name"
      val classNameNode = tc \ "className"
      val classNameSplit = classNameNode.text.split("\\.")
      val configurationName = classNameSplit.head
      val suiteName = classNameSplit.drop(1).headOption.getOrElse("STW")
      val className = if(classNameSplit.length > 1) classNameSplit.drop(2).mkString(".") else testName.text
      val status = tc \ "status"
      TestCase(new ObjectId(), build.number, testName.text, className, suiteName, configurationName, TestStatus.fromStringCaseInsensitive(status.text))
    })
  }
  
  def loadBuilds() = {
     val buildsXml = XML.load(ResultLoader.fromUrl("http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/api/xml"))
    val buildNodes = buildsXml \\ "build"
    val builds = buildNodes.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      Build(numberNode.text.toInt, urlNode.text)
    })
    builds
  }
}