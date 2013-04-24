package results
import scala.xml.XML

case class Build(number: Int, url: String)

class TestStatus
object Passed extends TestStatus {
  override def toString = "Passed"
}
object Failed extends TestStatus {
  override def toString = "Failed"
}
object Fixed extends TestStatus {
  override def toString = "Fixed"
}
object Regression extends TestStatus {
  override def toString = "Regression"
}

object TestStatus {
  def apply(status: String) = status.toUpperCase() match {
    case "PASSED" => Passed
    case "FAILED" => Failed
    case "FIXED" => Fixed
    case "REGRESSION" => Regression
  }

}

case class Test(testName: String, className: String, suiteName: String, configurationName: String, status: TestStatus)

object Results extends App {
  
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
      Test(testName.text, className, suiteName, configurationName, TestStatus(status.text))
    })
  }
  
  override def main(args: Array[String]) {
    
    val buildsXml = XML.load(ResultLoader.fromUrl("http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/api/xml"))
    val buildNodes = buildsXml \\ "build"
    val builds = buildNodes.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      Build(numberNode.text.toInt, urlNode.text)
    })
    println("builds: " + builds.toList)
    
    println("loading xml from server")
    val results = builds.map(build => (build.number, loadResultsForBuild(build)))
//    val xml = XML.load(ResultLoader.fromUrl("http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/835/testReport/api/xml"))
//    println("parsing")
//    val testcaseNodes = xml \\ "case"
//    val testcases = testcaseNodes.map(tc => {
//      val testName = tc \ "name"
//      val classNameNode = tc \ "className"
//      val classNameSplit = classNameNode.text.split("\\.")
//      val configurationName = classNameSplit.head
//      val suiteName = classNameSplit.drop(1).headOption.getOrElse("STW")
//      val className = if(classNameSplit.length > 1) classNameSplit.drop(2).mkString(".") else testName.text
//      val status = tc \ "status"
//      Test(testName.text, className, suiteName, configurationName, TestStatus(status.text))
//    })
//    println(testcases.size)
//    val tcByName = testcases.groupBy(_.testName).toMap
//    //println(tcByName.filter(_._2.forall(_.status == Failed)).mkString("\n"))
//    println(tcByName.size)
//    println(tcByName.filter(p => p._2.forall(_.status == Failed)).mkString("\n"))
  }
}