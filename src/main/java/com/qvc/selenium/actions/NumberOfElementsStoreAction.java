package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class NumberOfElementsStoreAction extends BaseAction {
	@Override
	public ExecuteResult runAction() throws UnhandledAlertException {

		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		String name = (String) testData.get("storeas"); //update by Simon, 10/29/2015
		List<WebElement> elms = getPageObject().getWebElements(getDriver(),this.getObjectName());
		data.setStoredData(name, elms);

		// Update test case data
		HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
		curTestCaseData.put(name, elms);
		data.setStoredData("testCaseData", curTestCaseData);

		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Store elements to: " + name);
		

		return stepResult;
	}
}
