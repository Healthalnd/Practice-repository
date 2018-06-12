package com.qvc.selenium.actions;

import java.util.List;

import org.openqa.selenium.WebElement;

import com.qvc.selenium.reporting.ExecuteResult;

public class VerifyObjectStatusAction extends BaseAction {
	@Override
	public ExecuteResult runAction() {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		boolean result = false;
		String status = null;
		String stepDetail = null;
		boolean disabled = false;
		boolean enabled = false;

		List<WebElement> elements = getPageObject().getWebElements(getDriver(),
				getObjectName());
		String action = (String) testData.get("action");
		switch (action.toLowerCase()) {
		case "verifybtndisabled":
			if (elements.size() == 0) {
				result = false;
				status = "Failed";
				stepDetail = "Element was not found, element - "
						+ getObjectName();

			} else {// with one more elements, check the element display or not
				for (WebElement e : elements) {
					disabled = e.isEnabled();
					if (disabled) {
						break;
					}
				}
				if (disabled) {// disabled - mean disabled
					result = false;
					status = "Failed";
					stepDetail = "Verify Button disabled, Element was abled, element - "
							+ getObjectName();
				} else {// not disabled - mean enabled
					result = true;
					status = "Passed";
					stepDetail = "Verify Button disabled, Element was disabled, element - "
							+ getObjectName();
				}
			}
			break;
		case "verifybtnenabled":
			if (elements.size() == 0) {
				result = false;
				status = "Failed";
				stepDetail = "Verify Button enabled, Element was not found, element - "
						+ getObjectName();

			} else {// with one more elements, check the element display or not
				for (WebElement e : elements) {
					enabled = e.isEnabled();
					if (enabled) {
						break;
					}
				}
				if (enabled) {// enabled - mean enabled
					result = true;
					status = "Passed";
					stepDetail = "Verify Button enabled, Element was enabled, element - "
							+ getObjectName();
				} else {// disabled - mean disabled
					result = false;
					status = "Failed";
					stepDetail = "Verify Button enabled, Element was disabled, element - "
							+ getObjectName();
				}
			}
			break;
		case "verifycheckboxselected":
			if (elements.size() == 0) {
				result = false;
				status = "Failed";
				stepDetail = "Verify Check Box selected, Element was not found, element - "
						+ getObjectName();

			} else {// with one more elements, check the element selected or not
				for (WebElement e : elements) {
					enabled = e.isSelected();
					if (enabled) {
						break;
					}
				}
				if (enabled) {// enabled - mean selected
					result = true;
					status = "Passed";
					stepDetail = "Verify Check Box selected, Element was selected, element - "
							+ getObjectName();
				} else {// disabled - mean disabled
					result = false;
					status = "Failed";
					stepDetail = "Verify Check Box selected, Element was unselected, element - "
							+ getObjectName();
				}
			}
			break;
		case "verifycheckboxunselected":
			if (elements.size() == 0) {
				result = false;
				status = "Failed";
				stepDetail = "Verify Check Box unselected, Element was not found, element - "
						+ getObjectName();

			} else {// with one more elements, check the element selected or not
				for (WebElement e : elements) {
					enabled = e.isSelected();
					if (enabled) {
						break;
					}
				}
				if (!enabled) {// disabled - mean unselected
					result = true;
					status = "Passed";
					stepDetail = "Verify Check Box unselected, Element was unselected, element - "
							+ getObjectName();
				} else {// enabled - mean disabled
					result = false;
					status = "Failed";
					stepDetail = "Verify Check Box unselected, Element was selected, element - "
							+ getObjectName();
				}
			}
			break;
			

			
		default:
			result = false;
			status = "failed";
			stepDetail = "Element was not found";

		}
		
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}
}
