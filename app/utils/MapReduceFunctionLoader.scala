package utils

import scala.io.Source

class MapReduceFunctionLoader(baseDir: String, commandName: String) {
  private[this] val dirName = baseDir + "/" + commandName

  lazy val map = Source.fromFile(dirName + "/map.js").mkString
  lazy val reduce = Source.fromFile(dirName + "/reduce.js").mkString
  lazy val finalizeFunction = Source.fromFile(dirName + "/finalize.js").mkString
}

object MapReduceFunctionLoader {
  def apply(baseDir: String, commandName: String) = 
    new MapReduceFunctionLoader(baseDir, commandName)
}