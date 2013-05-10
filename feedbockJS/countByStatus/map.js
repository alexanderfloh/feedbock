function() {
			var statusMapper = {
				"Passed": "passed",
				"Fixed": "passed",
				"Failed": "failed",
				"Regression": "failed"
			};
			
			var value = {
				passedTests: statusMapper[this.status.name] == "passed" ? 1 : 0,
				failedTests: statusMapper[this.status.name] == "failed" ? 1 : 0,
			}
			
			emit(this.buildNumber, value);
		}