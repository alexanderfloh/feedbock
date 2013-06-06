package results
import scala.xml.XML
import models.TestCase
import se.radley.plugin.salat.Binders._
import models.TestStatus
import java.net.URL
import play.api._
import models._
import services._
import scala.concurrent.duration.Duration
import scala.concurrent.Await

case class Build(number: Int, url: String)

object Results {
  val hostName = Play.current.configuration.getString("jenkins.hostName").getOrElse("http://localhost")
  val xmlApiSuffix = Play.current.configuration.getString("jenkins.xmlApiSuffix").getOrElse("")

  def fromUrl(url: String) = {
    val connection = new URL(url).openConnection()
    connection.getInputStream()
  }

  def loadMostRecentBuild(jobUrl: String) = {
    for {
      mostRecentBuild <- findMostRecentBuild(jobUrl)
    } yield {
      val triggeringBuild = findRootTriggerBuild(mostRecentBuild.url)
      Logger.info("triggering build " + triggeringBuild)
      val testcases = loadResultsForBuild(mostRecentBuild, triggeringBuild.number)
      Logger.info("collected results for build " + triggeringBuild.number + ", found " + testcases.size + " tests")
      (triggeringBuild.number, testcases)
      MongoService.saveMetaInformation(MetaInformation("mostRecentBuildNumber", triggeringBuild.number.toString))
    }
  }

  def loadResultsForBuild(build: Build, triggeringBuildNumber: Int) = {
    val xml = XML.load(fromUrl(build.url + "/testReport/api/xml"))
    println("parsing")
    val testcaseNodes = xml \\ "case"
    testcaseNodes.map(tc => {
      val testName = tc \ "name"
      val classNameNode = tc \ "className"
      val classNameSplit = classNameNode.text.split("\\.")
      val configurationName = classNameSplit.head
      val suiteName = classNameSplit.drop(1).headOption.getOrElse("STW")
      val className = if (classNameSplit.length > 1) classNameSplit.drop(2).mkString(".") else testName.text
      val status = tc \ "status"

      val key = TestCaseKey(suiteName, className, testName.text)

      var testCase = Await.result(MongoService.loadTestCaseByKey(key), Duration.Inf)
        .getOrElse(TestCase(key))

      var config = testCase.configurations.find(_.name == configurationName).getOrElse {
        val newConfig = TestCaseConfiguration(configurationName)
        testCase = testCase.withConfiguration(newConfig)
        newConfig
      }
      TestStatus.fromStringCaseInsensitive(status.text) match {
        case TestStatus("Passed") => config.passed = config.passed :+ triggeringBuildNumber
        case TestStatus("Fixed") => config.passed = config.passed :+ triggeringBuildNumber
        case TestStatus("Failed") => config.failed = config.failed :+ triggeringBuildNumber
        case TestStatus("Regression") => config.failed = config.failed :+ triggeringBuildNumber
      }

      MongoService.saveTestCase(testCase)
      testCase
    })
  }

  def findRootTriggerBuild(buildUrl: String): Build = {
    findRootTriggerBuildRec(Build(0, buildUrl))
  }

  private def findRootTriggerBuildRec(build: Build): Build = {
    findTriggeringBuild(build.url) match {
      case None => build
      case Some(triggeringBuild) => findRootTriggerBuildRec(triggeringBuild)
    }
  }

  /**
   * finds the triggering job for a certain build.
   * @param url url in the format http://hostname/job/jobName/&lt;buildNumber&gt;
   */
  def findTriggeringBuild(url: String) = {
    Logger.info(url)
    val buildsXml = XML.load(fromUrl(url + "/api/xml"))
    val cause = buildsXml \\ "cause"
    val upstreamUrl = cause \ "upstreamUrl"
    val upstreamBuild = cause \ "upstreamBuild"

    for {
      url <- upstreamUrl.headOption
      build <- upstreamBuild.headOption
    } yield Build(build.text.toInt, hostName + url.text + build.text)
  }

  def findMostRecentBuild(jobUrl: String) = {
    val buildsXml = XML.load(fromUrl(hostName + xmlApiSuffix))
    val mostRecentBuild = (buildsXml \\ "build").headOption
    mostRecentBuild.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      val b = Build(numberNode.text.toInt, urlNode.text)
      Logger.info("found most recent build " + b)
      b
    })
  }

  def loadBuilds() = {
    val buildsXml = XML.load(fromUrl(hostName + xmlApiSuffix))
    val buildNodes = buildsXml \\ "build"
    val builds = buildNodes.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      Build(numberNode.text.toInt, urlNode.text)
    })
    builds
  }
}