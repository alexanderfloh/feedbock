function() {
	var value = {
	  defect : 0,
	  codeChange : 0,
	  timing: 0
	};
	
	this.feedback.map(function(feedback) {
		if(feedback.defect) {
			value.defect += 1;
		}
		if(feedback.codeChange) {
			value.codeChange += 1;
		}
		if(feedback.timingIssue) {
			value.timing += 1;
		}
	});

	emit({
		suiteName: this._id.suiteName,
		className: this._id.className,
		testName: this._id.testName
	}, value);
}