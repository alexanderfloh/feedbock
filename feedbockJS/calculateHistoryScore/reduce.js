function(key, values) {
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
		}