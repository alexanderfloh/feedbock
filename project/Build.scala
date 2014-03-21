import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "feedbock"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    //jdbc,
    //anorm,
    //"org.reactivemongo" %% "reactivemongo" % "0.9",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0" exclude("org.scala-stm", "scala-stm_2.10.0"),

    "org.jvnet.com4j" % "com4j" % "20120426-2",
    "org.jvnet.com4j.typelibs" % "ado20" % "1.0",
    "org.jvnet.com4j.typelibs" % "active-directory" % "1.0",
    "no.arktekk" % "anti-xml_2.10" % "0.5.1")
    

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here     
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      "Jenkins Releases" at "http://maven.jenkins-ci.org/content/repositories/releases")
      )

}
