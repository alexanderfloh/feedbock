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
			//emit({suite: this.suiteName, build: this.buildNumber}, value);
			emit({suite: this.suiteName}, value);
			//emit({suite: "all"}, value);
		},
		reduce: function(key, values) {
			var reducedVal = {};
			values.forEach(function(val) {
				for (var member in val) {
					var currentVal = reducedVal[member] || 0;
					currentVal += val[member];
					val[member] = currentVal;
					reducedVal[member] = currentVal;
				}
			});
			return reducedVal;
		},
		finalize: function(key, reducedVal) {
			var passed = reducedVal.passed || 0;
			var failed = reducedVal.failed || 0;
			reducedVal.sum = passed + failed;
			var factor = reducedVal.sum / 100;
			reducedVal.failedPercent = failed / factor;
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