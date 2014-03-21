package services.sc

import play.api.libs.ws.WS
import play.api.Play

object RequestBuilder {
	def apply(reportId: String, additionalParams: (String, String)*) = {
	  val config = Play.current.configuration
	  val url = config.getString("sc.url").get
	  val user = config.getString("sc.user").get
	  val password = config.getString("sc.password").get
	  val projectId = config.getString("sc.projectId").get
	  
	  val mergedParams = additionalParams ++ Seq(
	      ("hid", "reportData"),
          ("type", "xml"),
          ("userName", user),
          ("passWord", password),
          ("projectID", projectId),
          ("reportFilterID", reportId)
	  )
	  
	  WS.url(url).withQueryString(mergedParams:_*)
	}
}