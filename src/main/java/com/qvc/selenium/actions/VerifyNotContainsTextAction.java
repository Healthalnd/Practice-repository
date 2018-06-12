package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.WebElement;

public class VerifyNotContainsTextAction extends BaseAction {

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		boolean result = false;
		String status = null;
		String stepDetail = null;
		String actText = null;
		String expText = null;
		try{
			WebElement object = this.getPageObject().getWebElement(getDriver(),
					getObjectName());
			if (object != null) {
				actText = object.getText();
				expText = (String) testData.get("text");
				if(actText == null || !actText.contains(expText)){//not contain
					result = true;
					status = "Passed";
					stepDetail = "Expect text was not found: " + expText;
				}else{
					result = false;
					status = "Failed";
					stepDetail = "Expect text was found: " + expText + ", actul text: " + actText;
	                logger.error("Verification Failed. " + stepDetail);
				}
			}
			
		} catch (Exception e){
			result = true;//this is set for For loop -> LoopTestStep.java
			status = "Failed";
			stepDetail = "Element was not found, element - " + getObjectName();
			logger.info(stepDetail + ", ignore the exception for loop condition");
		}
		

		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;

	}
}
