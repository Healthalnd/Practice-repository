package com.qvc.selenium.actions;

import org.openqa.selenium.WebElement;

import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;
import com.qvc.selenium.reporting.ExecuteResult;

public class VerifyContainsElementsAction extends BaseAction {

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();

		boolean result = false;
		String status = "Failed";
		String stepDetail = "Element was not found, element - " + getObjectName();
		String checkStatus = (String) testData.get("checkbox");//for mobile -if checkbox exist and was checked status - will uncheck it(Simon, 8/1/2016)
		try {
			setTimeout(5000);//Set time out to 5 seconds for if condition element check(default was 10 seconds)
			WebElement el = getWebElement();
			
			result = true;
			status = "Passed";
			stepDetail = "Element was found, element - " + getObjectName();
			
			if(checkStatus != null && checkStatus.equalsIgnoreCase("checked")){//used for IfContains statement, when the checkbox was checked, uncheck it
				if (getDriver() instanceof QVCAndroidDriver) {
					if(el.getAttribute("checked").equalsIgnoreCase("false")){
						result = false;
						status = "Failed";
						stepDetail = "Checkbox was unchecked - " + getObjectName();
					}
				} else if (getDriver() instanceof QVCiOSDriver) {
					if(!el.getAttribute("value").equals("1")){// value: empty mean unchecked, value: 1 mean checked for iPhone
						result = false;
						status = "Failed";
						stepDetail = "Checkbox was unchecked - " + getObjectName();
						}
					}
			}else if(checkStatus != null && checkStatus.equalsIgnoreCase("unchecked")){////used for IfContains statement, when the checkbox was Unchecked, check it
				if (getDriver() instanceof QVCAndroidDriver) {
					if(el.getAttribute("checked").equalsIgnoreCase("true")){
						result = false;
						status = "Failed";
						stepDetail = "Checkbox was checked - " + getObjectName();
					}
				} else if (getDriver() instanceof QVCiOSDriver) {
					if(el.getAttribute("value").equals("1")){// value: empty mean unchecked, value: 1 mean checked for iPhone
						result = false;
						status = "Failed";
						stepDetail = "Checkbox was checked - " + getObjectName();
						}
					}
			}
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}

		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}
}
