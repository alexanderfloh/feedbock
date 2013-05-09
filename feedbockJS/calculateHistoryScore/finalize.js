function(key, reducedVal) {
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
		}