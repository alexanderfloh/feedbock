function(key, values) {
			var reduced = {
				passedTests: 0,
				failedTests: 0
			}
			values.forEach(function(val) {
				reduced.passedTests += val.passedTests;
				reduced.failedTests += val.failedTests;
			});
			return reduced;
		}