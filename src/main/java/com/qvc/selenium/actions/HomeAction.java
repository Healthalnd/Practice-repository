package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

import io.appium.java_client.ios.IOSDriver;
import org.apache.log4j.Logger;

public class HomeAction extends BaseAction {
	static final Logger logger = Logger.getLogger(HomeAction.class.getName());

	@Override
	public ExecuteResult runAction() {
		logger.debug("in Home action");
		if (getDriver() instanceof IOSDriver){
			((IOSDriver) getDriver()).runAppInBackground(3);
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
		}
		else if (getDriver() instanceof AndroidDriver){
			((AndroidDriver) getDriver()).sendKeyEvent(AndroidKeyCode.HOME);
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
		}
		else
		{
			logger.fatal("Home action is applicable only for Android emulator.");
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
		}
		 
		stepResult.setStepDetail("Home action");
		return stepResult;
	}
}
