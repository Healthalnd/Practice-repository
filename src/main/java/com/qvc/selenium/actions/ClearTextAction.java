package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import com.qvc.selenium.steps.LoopTestStep;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

public class ClearTextAction extends BaseAction {
	static final Logger logger = Logger.getLogger(LoopTestStep.class.getName());

	@Override
	public ExecuteResult runAction() throws Exception {
		ExecuteResult stepResult = new ExecuteResult();
		String objectName = getObjectName();
		WebElement curElemt = getWebElement();
		logger.debug("Clear text for " + objectName);
		curElemt.clear();
		
		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Clear text for input field" + objectName);
		return stepResult;
	}

}
