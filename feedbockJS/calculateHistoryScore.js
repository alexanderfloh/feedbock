(function(mongodb) {
	"use strict";

	var serverFunctions = {
		map: function() {
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
		},
		reduce: function(key, values) {
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
		},
		finalize: function(key, reducedVal) {
			var calcMax = function(arr) {
				return arr.length ? Math.max.apply(Math, arr) : 0;
			};
			reducedVal.builds.latestPassed = calcMax(reducedVal.builds.passed);
			reducedVal.builds.latestFailed = calcMax(reducedVal.builds.failed);
			reducedVal.historyScore = reducedVal.builds.latestFailed - reducedVal.builds.latestPassed;
			if (reducedVal.historyScore > 0) {
				return reducedVal;
			}
			return null;
		}
	};

	var mongoServer = mongodb.Server("localhost", 27017, {});
	var db = mongodb.Db("feedbock", mongoServer, { safe: false });
	db.open(function(error, client) {
		if (error) {
			throw error;
		}
		console.log("go!");
		db.executeDbCommand({
			mapreduce: "testCases", 
			out:  { inline: 1 },
			query: {
				suiteName: "xBrowser - Recording"
			},
			map: serverFunctions.map.toString(),
			reduce: serverFunctions.reduce.toString(),
			finalize: serverFunctions.finalize.toString()
		},
		function(err, dbres) {
			var results = dbres.documents[0].results;
			console.log(JSON.stringify(results, null, "\t"));
			db.close();
		});
	});
})(
	require("mongodb")
);