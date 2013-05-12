function(key, reducedVal) {
  var score = 10 + 
    reducedVal.defect * 10 + 
    reducedVal.codeChange * 5 +
    reducedVal.timing * -2;
  return Math.max(score, 0);  
}