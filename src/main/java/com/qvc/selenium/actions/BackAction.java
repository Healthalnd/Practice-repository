package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.log4j.Logger;

import io.appium.java_client.android.AndroidKeyCode;

public class BackAction extends BaseAction {
	static final Logger logger = Logger.getLogger(BackAction.class.getName());

	@Override
	public ExecuteResult runAction() {
		logger.debug("in Back action");
		if (getDriver() instanceof IOSDriver){
			logger.fatal("Back action is not applicable for iOS.");
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
		}
		else if (getDriver() instanceof AndroidDriver){
            // hide keyboard if exist
            try {
                ((AppiumDriver) getDriver()).hideKeyboard();
            } catch (Exception e) {
                // keyboard doesn`t exist
            }
			((AndroidDriver) getDriver()).sendKeyEvent(AndroidKeyCode.BACK);
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
		}
		else
		{
			getDriver().navigate().back();
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
		}
		
		stepResult.setStepDetail("Go back");
		return stepResult;
	}
}
