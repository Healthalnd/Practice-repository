package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class WebTabletAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(WebTabletAction.class
			.getName());

	private WebElement table;
	private WebElement tableBody;
	private List<WebElement> rows;
	private List<WebElement> columns;
	private int rowCount;
	boolean result = false;
	String status = null;
	String stepDetail = null;

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		// use to take case the WebTable
		table = getPageObject()
				.getWebElement(getDriver(), this.getObjectName());
		tableBody = table.findElement(By.tagName("tbody"));
		rows = tableBody.findElements(By.tagName("tr"));
		switch ((String) testData.get("tableaction")) {
		case "outputspecificrowcount":
			outputSpecificRowCount(
					Integer.parseInt(testData.get("column").toString()),
					(String) testData.get("celltext"));
			break;
		case "clickaction":
			clickAction(Integer.parseInt(testData.get("row").toString()),
					Integer.parseInt(testData.get("column").toString()));
			break;
		case "VerifyCellData":
			VerifyCellData(
					Integer.parseInt(testData.get("row").toString()),
					Integer.parseInt(testData.get("column").toString()),
					(String) testData.get("celltext"));
			break;
		}
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}

	public int getRowCount() {
		return rows.size();

	}

	private void outputSpecificRowCount(int column, String expText) {
		boolean isFound = false;
		rowCount = getRowCount();
		for (int i = 0; i < rowCount; i++) {
			// rows = tableBody.findElements(By.tagName("tr"));
			columns = rows.get(i).findElements(By.tagName("td"));
			if (columns.get(column).getText().toLowerCase().contains(expText.toLowerCase())) {
				testData.put("row", i);
				 // Update test case data
		        HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
		        curTestCaseData.put("row", i);
		        data.setStoredData("testCaseData", curTestCaseData);
		        
				isFound = true;
				
				result = true;
				status = "Passed";
				stepDetail = "ExpText was found - " + expText + ", on webTable->column: " + column + ", row: " + i;
				break;
			}
		}
		
		if (!isFound) {
			result = true;
			status = "Failed";
			stepDetail = "can't find the text:" + expText;
			
			logger.debug("can't find the text:" + expText);
		}
	}

	private void clickAction(int row, int column) {

		// rows = tableBody.findElements(By.tagName("tr"));
		columns = rows.get(row).findElements(By.tagName("td"));
		if (columns.get(column).findElement(By.tagName("input")) != null) {
			columns.get(column).findElement(By.tagName("input")).click();
			result = true;
			status = "Passed";
			stepDetail = "Click element->input on table: column - " + column + ", row - " + row;
		} else if (columns.get(column).findElement(By.tagName("a")) != null) {
			columns.get(column).findElement(By.tagName("a")).click();
			result = true;
			status = "Passed";
			stepDetail = "Click element->a on table: column - " + column + ", row - " + row;
		}else {
			result = false;
			status = "failed";
			stepDetail = "Click fail(input or a) on table: column - " + column + ", row - " + row;
		}
	}

	private void VerifyCellData(int row, int column, String expText) {
		columns = rows.get(row).findElements(By.tagName("td"));
		if (columns.get(column).getText().contains(expText)) {
			result = true;
			status = "Passed";
			stepDetail = "verify cell data successful,the expected text is "+ expText + " the actual text is "+ columns.get(column).getText();
			
			logger.debug("verify cell data successful,the expected text is "
					+ expText + " the actual text is "
					+ columns.get(column).getText());
		} else {
			result = false;
			status = "Failed";
			stepDetail = "verify cell data failed,the expected text is "+ expText + " the actual text is "+ columns.get(column).getText();
			
			logger.debug("verify cell data failed,the expected text is "
					+ expText + " the actual text is "
					+ columns.get(column).getText());
		}

	}
}
