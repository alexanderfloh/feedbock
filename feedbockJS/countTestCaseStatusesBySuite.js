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
			var value = {};
			value[statusMapper[this.status.name]] = 1;
			emit({suite: this.suiteName}, value);
			//emit({suite: this.suiteName, build: this.buildNumber}, value);
			//emit({suite: "all"}, value);
		},
		reduce: function(key, values) {
			var reducedVal = {};
			values.forEach(function(val) {
				for (var member in val) {
					var currentVal = reducedVal[member] || 0;
					currentVal += val[member];
					reducedVal[member] = currentVal;
				}
			});
			return reducedVal;
		},
		finalize: function(key, reducedVal) {
			reducedVal.passed = reducedVal.passed || 0;
			reducedVal.failed = reducedVal.failed || 0;
			reducedVal.sum = reducedVal.passed + reducedVal.failed;
			if (reducedVal.passed === reducedVal.sum) {
				// prevent calculation errors and set 0% failed and 100% passed
				reducedVal.failedPercent = 0;
				reducedVal.passedPercent = 100;
			} else if (reducedVal.failed === reducedVal.sum) {
				// prevent calculation errors and set 100% failed and 0% passed
				reducedVal.failedPercent = 100;
				reducedVal.passedPercent = 0;
			} else {
				var factor = reducedVal.sum / 100;
				reducedVal.failedPercent = reducedVal.failed / factor;
				reducedVal.passedPercent = reducedVal.passed / factor;				
			}
			return reducedVal;
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