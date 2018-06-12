package com.qvc.selenium.reporting;

public class ExecuteResult {
	private static final String TEST_RESULT_PASSED = "Passed";
	private static final String TEST_RESULT_FAILED = "Failed";
	
	private String status;
	private String stepDetail;
	private boolean result;
	
	
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		if(status.equalsIgnoreCase(TEST_RESULT_PASSED)){
			this.status = TEST_RESULT_PASSED;
		}else {
			this.status = TEST_RESULT_FAILED;
		}
	}
	public String getStepDetail() {
		return stepDetail;
	}
	public void setStepDetail(String stepDetail) {
		this.stepDetail = stepDetail;
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
		
}
