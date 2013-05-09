function() {
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
		}