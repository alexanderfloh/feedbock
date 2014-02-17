package results
import scala.xml.XML
import models.TestCase
import models.TestStatus
import java.net.URL
import play.api._
import models._
import services._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import utils.Timer

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
      val testcases = loadResultsForBuild(mostRecentBuild, triggeringBuild.number)
      Logger.info(s"collected results for build ${triggeringBuild.number}")
      MongoService.saveMetaInformation(MetaInformation("mostRecentBuildNumber", triggeringBuild.number.toString))
      triggeringBuild.number
    }
  }

  def loadResultsForTestRun(testRunBuildNumber: Int) = {
    for (build <- loadBuild(testRunBuildNumber)) yield {
      val triggeringBuild = findRootTriggerBuild(build.url)
      loadResultsForBuild(build, triggeringBuild.number)
    }
  }

  def loadResultsForBuild(build: Build, triggeringBuildNumber: Int) = Timer("load results"){
    val xml = Timer("load xml from server")(XML.load(fromUrl(build.url + "/testReport/api/xml")))
    val testcaseNodes = xml \\ "case"
    testcaseNodes.foreach(tc => {
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
        case Passed => config.passed = config.passed + triggeringBuildNumber
        case Failed => config.failed = config.failed + triggeringBuildNumber
      }

      MongoService.saveTestCase(testCase)
    })
  }

  def findRootTriggerBuild(buildUrl: String): Build = Timer("find root triggering build"){
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
    val buildsXml = XML.load(fromUrl(url + "/api/xml"))
    val cause = buildsXml \\ "cause"
    val upstreamUrl = cause \ "upstreamUrl"
    val upstreamBuild = cause \ "upstreamBuild"

    for {
      url <- upstreamUrl.headOption
      build <- upstreamBuild.headOption
    } yield Build(build.text.toInt, hostName + url.text + build.text)
  }

  def findMostRecentBuild(jobUrl: String) = Timer("find most recent build"){
    val buildsXml = XML.load(fromUrl(hostName + xmlApiSuffix))
    val mostRecentBuild = (buildsXml \\ "build").headOption
    mostRecentBuild.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      val b = Build(numberNode.text.toInt, urlNode.text)
      Logger.debug(s"found most recent build $b")
      b
    })
  }

  def loadBuild(testRunBuildNumber: Int) = loadBuilds.find(_.number == testRunBuildNumber)

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