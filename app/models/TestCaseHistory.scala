package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import play.libs.Json._
import com.mongodb.casbah.map_reduce.MapReduceCommand
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput
import com.mongodb.casbah.map_reduce.MapReduceInlineOutput

case class TestCaseHistory(
  buildNumber: Int,
  className: String,
  suiteName: String,
  testName: String,
  comment: String,
  timestamp: DateTime,
  additionalData: Map[String, String]) {

  def isDefect = additionalData.get("defect").map(_.toBoolean).getOrElse(false)
  def isCodeChange = additionalData.get("codeChange").map(_.toBoolean).getOrElse(false)
  def isTiming = additionalData.get("timing").map(_.toBoolean).getOrElse(false)
}

object TestCaseHistory extends ModelCompanion[TestCaseHistory, ObjectId] {
  def collection = mongoCollection("testCaseHistory")
  val dao = new SalatDAO[TestCaseHistory, ObjectId](collection) {}

  def getHistoryByTestCase(testCase: TestCase): List[TestCaseHistory] = {
    dao.find(MongoDBObject("testName" -> testCase.testName, "className" -> testCase.className, "suiteName" -> testCase.suiteName)).sort(orderBy = MongoDBObject("timestamp" -> -1)).toList
  }

  val map = """function() {
			var statusMapper = {
				"Passed": "passed",
				"Failed": "failed",
				"Fixed": "passed",
				"Regression": "failed"
			};
			var value = {
				builds: {
					passed: [],
					failed: []
				}
			};
			switch(statusMapper[this.status.name]) {
				case "passed":
					value.builds.passed.push(this.buildNumber);
					break;
				case "failed":
					value.builds.failed.push(this.buildNumber);
					break;
			}
			emit({
				suite: this.suiteName,
				clazz: this.className,
				test: this.testName
			}, value);
		}"""

  val reduce = """function(key, values) {
			var reducedVal = {
				builds: {
					passed: [],
					failed: []
				}
			};
			values.forEach(function(val) {
				val.builds.passed.forEach(function(build) {
					if (reducedVal.builds.passed.indexOf(build) < 0) {
						reducedVal.builds.passed.push(build);
					}
				});
				val.builds.failed.forEach(function(build) {
					if (reducedVal.builds.failed.indexOf(build) < 0) {
						reducedVal.builds.failed.push(build);
					}
				});
			});
			return reducedVal;
		}"""

  val finalizeFunction = """function(key, reducedVal) {
			var calcMax = function(arr) {
				return arr.length ? Math.max.apply(Math, arr) : 0;
			};
			reducedVal.builds.latestPassed = calcMax(reducedVal.builds.passed);
			reducedVal.builds.latestFailed = calcMax(reducedVal.builds.failed);
			reducedVal.historyScore = reducedVal.builds.latestFailed - reducedVal.builds.latestPassed;
			if (reducedVal.historyScore > 0) {
				return reducedVal;
			}
			return 0;
		}"""

  def calculateScore() = {
    val mrc = MapReduceCommand(
      input = "testCases",
      map = map,
      reduce = reduce,
      finalizeFunction = Some(finalizeFunction),
      output = MapReduceInlineOutput)
   collection.mapReduce(mrc).toList   
  }
}

