package com.qvc.selenium.actions;

import java.util.List;

import org.openqa.selenium.WebElement;

import com.qvc.selenium.reporting.ExecuteResult;

public class VerifyNonexistenceAction extends BaseAction {
	@Override
	public ExecuteResult runAction() {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		boolean result = false;
		String status = null;
		String stepDetail = null;
		boolean displayed = false;
		timeout = 5000;

		// add switch code to control running for Web or for Mobile, Simon, 3/3/2016
		String platform = (String) data.getStoredData("app");
		if (platform.toLowerCase().contains("web"))
			platform = "web";

		switch (platform) {
		case "web": // for Web
			List<WebElement> elements = getPageObject().getWebElements(
					getDriver(), getObjectName());

			if (elements.size() == 0) {
				result = true;
				status = "Passed";
				stepDetail = "Element was not exist, element - "
						+ getObjectName();

			} else {// with one more elements, check the element display or not
				for (WebElement e : elements) {
					displayed = e.isDisplayed();
					if (displayed) {
						break;
					}
				}
				
				if (displayed) {// displayed - mean exist
					result = false;
					status = "Failed";
					stepDetail = "Element was exist, element - "
							+ getObjectName();
				} else {// not displayed - mean not exist
					result = true;
					status = "Passed";
					stepDetail = "Element was not exist, element - "
							+ getObjectName();
				}
			}
			break;

		default: // for Mobile
			try {
				getWebElement(false);
				result = false;
				status = "Failed";
				stepDetail = "Element was found, element - " + getObjectName();
				logger.error("Verification Failed. " + stepDetail);
			} catch (Exception e) {
				result = true;
				status = "Passed";
				stepDetail = "Element was not found, element - "
						+ getObjectName();
			}

		}// end switch

		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}
}
