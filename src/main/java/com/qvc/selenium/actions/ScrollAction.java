package com.qvc.selenium.actions;

import org.openqa.selenium.JavascriptExecutor;

import com.qvc.selenium.reporting.ExecuteResult;

public class ScrollAction extends BaseAction{

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		//getWebElement();
		String action = (String) testData.get("action");
		switch (action.toLowerCase()) {
		case "downbycorrdinate":
			//getDriver().executeScript("window.scrollTo(0, document.body.scrollHeight)");
			getDriver().executeScript("scroll(0,1000);");
			/*JavascriptExecutor js = ((JavascriptExecutor) getDriver());
			js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
			getDriver().executeScript("scroll(0,1000);");*/
			break;
		} 

		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Scroll Action to label: " + data.getStoredData("label"));
		return stepResult;

	}
}
