package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

import org.apache.log4j.Logger;

public class OpenMenuAction extends BaseAction {
	static final Logger logger = Logger.getLogger(OpenMenuAction.class.getName());

	@Override
	public ExecuteResult runAction() {
		logger.debug("in open menu action");
		if (getDriver() instanceof AndroidDriver){
			((AndroidDriver) getDriver()).sendKeyEvent(AndroidKeyCode.MENU);
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
		}
		else
		{
			logger.fatal("Menu action is applicable only for Android emulator.");
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
		}
		
		stepResult.setStepDetail("Open menu");
		return stepResult;
	}
}
