function() {
	value = {
	  defect : 0,
	  codeChange : 0,
	  timing: 0
	};
	
	if(this.additionalData.defect == "true") {
		value.defect = 1;
	}
	if(this.additionalData.codeChange == "true") {
		value.codeChange = 1;
	}
	if(this.additionalData.timing == "true") {
		value.timing = 1;
	}
	
	emit({
		suiteName: this.suiteName,
		className: this.className,
		testName: this.testName
	}, value);
}