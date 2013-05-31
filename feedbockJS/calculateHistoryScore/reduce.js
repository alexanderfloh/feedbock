function(key, values) {
			var reducedVal = {
			  defect : 0,
			  codeChange : 0,
			  timing: 0
			};
			values.map(function(val) {
				reducedVal.defect += val.defect;
				reducedVal.codeChange += val.codeChange;
				reducedVal.timing += val.timing;
			});
			return reducedVal;
		}