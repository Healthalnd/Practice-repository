package com.qvc.selenium.actions;

import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.interactions.Actions;

import com.qvc.selenium.reporting.ExecuteResult;

public class BrowserAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(BrowserAction.class
			.getName());
	boolean result = false;
	boolean found = false;
	String status = null;
	String stepDetail = null;
	Actions actionBuilder;
	String parentWindow;
	Set<String> handles;

	/**
	 * use for browser operation, such as: open, close, naviagtion.back, forward
	 */
	public ExecuteResult runAction() throws UnhandledAlertException {
		super.logAction();
		// String platform;
		// String targetTestServer;
		// String browserType;
		// RemoteWebDriver driver;
		ExecuteResult stepResult = new ExecuteResult();
		String action = (String) testData.get("action");
		switch (action.toLowerCase()) {
		case "clearcookies": //Clear cookies
			
			getDriver().manage().deleteAllCookies();
			
			result = true;
			status = "Passed";
			stepDetail = "Cookie add successful";
			logger.debug("Cookie add successful");
			break;
		case "close":
			getDriver().quit();

			result = true;
			status = "Passed";
			stepDetail = "Browser was closed";
			logger.debug("Browser was closed");
			break;
		case "back":
			getDriver().navigate().back();

			result = true;
			status = "Passed";
			stepDetail = "Browser was navigate back";
			logger.debug("Browser was navigate back");
			break;
		case "forward":
			getDriver().navigate().forward();

			result = true;
			status = "Passed";
			stepDetail = "Broswer was navigate forward";
			logger.debug("Broswer was navigate forward");
			break;
		case "refresh":
			getDriver().navigate().refresh();

			result = true;
			status = "Passed";
			stepDetail = "Broswer was refresh";
			logger.debug("Broswer was refresh");
			break;
		case "addcookie":// for AEM IT and FR
			String TestCountry = (String) data.getStoredData("ui");

			Cookie ck = new Cookie(
					"oreo",
					"0f517790bb1534cadb55c2f74a63244ecf8b95537ec61f242141f1e3bca90c54",
					".qvc." + TestCountry, "/", null);
			getDriver().manage().addCookie(ck);

			result = true;
			status = "Passed";
			stepDetail = "Cookie add successful";
			logger.debug("Cookie add successful");
			break;
		case "acceptalert":// take care Alert - click Yes
			Alert alert = getDriver().switchTo().alert();
			String alertText = alert.getText();
			alert.accept();

			result = true;
			status = "Passed";
			stepDetail = alertText + " - Click Yes";
			logger.debug(alertText + " - Click Yes");
			break;
		case "open":

			// Code not implement
			result = true;
			status = "Failed";
			stepDetail = "Browser open was not implement";
			logger.debug("Browser was reopened");
			break;

		case "verifytitle":// verify page name/title
			String expTitle = (String) testData.get("text");
			String actTitle = getDriver().getTitle();
			if (actTitle.equalsIgnoreCase(expTitle)) {
				result = true;
				status = "Passed";
				stepDetail = "Page title was verified as expected - "
						+ expTitle;
			} else {
				result = true;
				status = "Failed";
				stepDetail = "Page title verify failed - Exp: " + expTitle
						+ ", Act: " + actTitle;
			}
			break;

		case "switchtonewbrowser":// Switch to new(second) browser
			//Thread.sleep(5000);
			parentWindow = getDriver().getWindowHandle();
			handles = getDriver().getWindowHandles();
			for (String windowHandle : handles) {
				if (!windowHandle.equals(parentWindow)) {
					getDriver().switchTo().window(windowHandle);
					found = true;
					logger.info("Switch to new browser, windowHandle - " + windowHandle);
				}
			}
			
			if(found){
				result = true;
				status = "Passed";
				stepDetail = "Switch to new(second) browser successful";
			}else{
				result = true;
				status = "Failed";
				stepDetail = "Not found the new browser";
			}

			break;

		case "closenewbrowser":// close the new(second) browser
			parentWindow = getDriver().getWindowHandle();
			handles = getDriver().getWindowHandles();
			for (String windowHandle : handles) {
				if (!windowHandle.equals(parentWindow)) {
					getDriver().close();
					getDriver().switchTo().window(windowHandle);//back to 1st browser
					found = true;
					logger.info("Close the new browser, windowHandle - " + windowHandle);
				}
			}
						
			if(found){
				result = true;
				status = "Passed";
				stepDetail = "Close the new(second) browser successful";
			}else{
				result = true;
				status = "Failed";
				stepDetail = "Not found the new browser";
			} 

			break;

		default:
			result = false;
			status = "Failed";
			stepDetail = "Browser operation failed - no vaule set for action";
			logger.debug("Browser operation failed - no vaule set for action");

		}
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;

	}
}
