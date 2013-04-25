package results
import scala.xml.XML
import models.TestCase
import se.radley.plugin.salat.Binders._
import models.TestStatus
import java.net.URL
import play.api._

case class Build(number: Int, url: String)

object Results {
  val hostName = "http://lnz-bobthebuilder/hudson/"

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
      TestCase(new ObjectId(), triggeringBuildNumber, testName.text, className, suiteName, configurationName, TestStatus.fromStringCaseInsensitive(status.text))
    })
  }

  def findRootTriggerBuild(buildUrl: String): Build = {
    findRootTriggerBuildRec(Build(0, buildUrl))
  }
  
  private def findRootTriggerBuildRec(build: Build) : Build = {
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
    val buildsXml = XML.load(fromUrl(hostName + "job/Trigger%20BVT%20Testset%20AllInOne/api/xml"))
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
    val buildsXml = XML.load(fromUrl(hostName + "job/Trigger%20BVT%20Testset%20AllInOne/api/xml"))
    val buildNodes = buildsXml \\ "build"
    val builds = buildNodes.map(node => {
      val numberNode = node \ "number"
      val urlNode = node \ "url"
      Build(numberNode.text.toInt, urlNode.text)
    })
    builds
  }
}