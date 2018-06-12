package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.apache.log4j.Logger;

public class GoAction extends BaseAction {
	static final Logger logger = Logger.getLogger(GoAction.class.getName());

	@Override
	public ExecuteResult runAction() {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		String url = (String) testData.get("url");
		getDriver().get(url);
		
		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Nav to URL: " + url);
//		try {
//			Assert.assertEquals(getDriver().getCurrentUrl(),
//					(String) data.getStoredData("url"));
//			
//			
//			logger.debug("We dun ran an assert on :"
//					+ "getDriver().getCurrentUrl()" + "getInput()");
//		} catch (java.lang.AssertionError a) {
//			logger.debug(a);
//		}
		return stepResult;
	}

}
