package results

import java.net.HttpURLConnection
import java.net.URL
import scala.io.Source

class ResultLoader {

}

object ResultLoader extends App {
  def fromUrl(url: String) = {
    val connection = new URL(url).openConnection()
    connection.getInputStream()
  }

  override def main(args: Array[String]) {
	  println(fromUrl("http://lnz-bobthebuilder/hudson/job/Trigger%20BVT%20Testset%20AllInOne/804/testReport/api/xml"))
  }
}