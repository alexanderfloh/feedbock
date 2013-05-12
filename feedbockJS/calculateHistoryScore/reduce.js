function(key, values) {
			var reducedVal = {
			  defect : 0,
			  codeChange : 0,
			  timing: 0
			};
			values.forEach(function(val) {
				reducedVal.defect += val.defect;
				reducedVal.codeChange += val.codeChange;
				reducedVal.timing += val.timing;
			  //Object.keys(val).forEach(function(key){
			//	 reducedVal[key] += val[key]; 
			 // });
			});
			return reducedVal;
		}