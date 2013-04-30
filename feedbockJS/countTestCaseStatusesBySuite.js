(function(mongodb) {
	"use strict";
	var mongoServer = mongodb.Server("localhost", 27017, {});
	var db = mongodb.Db("feedbock", mongoServer, { safe: false });
	db.open(function(error, client) {
		if (error) {
			throw error;
		}
		console.log("go!");
		var mapFn = function() {
			var value = {};
			value[this.status.name] = 1;
			emit(this.suiteName, value);
		};
		var reduceFn = function(key, values) {
			var reducedValue = {};
			values.forEach(function(val) {
				for (var member in val) {
					var currentVal = reducedValue[member] || 0;
					currentVal += val[member];
					val[member] = currentVal;
					reducedValue[member] = currentVal;
				}
			});
			return reducedValue;
		};
		db.executeDbCommand({
			mapreduce: "testCases", 
			out:  { inline: 1 },
			map: mapFn.toString(),
			reduce: reduceFn.toString()
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