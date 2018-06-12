package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;


public class CSSContainsAssert extends BaseAction {

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		
		String actcssValue = getPageObject().getWebElement(getDriver(), getObjectName())
				.getCssValue((String) data.getStoredData("cssName"));
		String extcssValue = ((String) data.getStoredData("cssName"));
		
		if(actcssValue.contains(extcssValue)){
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
			stepResult.setStepDetail("Expect css value was found: " + extcssValue + ", actul css value: " + actcssValue);
		}else{
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
			stepResult.setStepDetail("Expect css value was not found: " + extcssValue + ", actul css value: " + actcssValue);
		}
		return stepResult;

	}
}
