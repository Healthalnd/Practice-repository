package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;
import org.apache.log4j.Logger;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;

public class MouseEventAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(MouseEventAction.class
			.getName());

	boolean result = false;
	String status = null;
	String stepDetail = null;
	Actions actionBuilder;

	/**
	 * use for mouse event, such as: mouse over, double click...
	 */
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		switch ((String) testData.get("mouseaction")) {
		case "mouseover":
			actionBuilder = new Actions(getDriver());
			WebElement curElemt = this.getPageObject().getWebElement(getDriver(), getObjectName());
			logger.info("scrollIntoView - focus on element before mouse over it");
	        ((JavascriptExecutor)getDriver()).executeScript("arguments[0].scrollIntoView(true);", curElemt);
	        Thread.sleep(2000);
			actionBuilder.moveToElement(curElemt).build().perform();

			result = true;
			status = "Passed";
			stepDetail = "Mouse over to object:  " + getObjectName();
			logger.debug("Mouse over to object:  " + getObjectName());
			break;
		default:
			result = false;
			status = "Failed";
			stepDetail = "Mouse over failed - no vaule set for mouseaction";
			logger.debug("Mouse over failed - no vaule set for mouseaction");
		
		}
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}

}
