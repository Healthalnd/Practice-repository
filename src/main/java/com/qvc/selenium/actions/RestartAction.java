package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.WebElement;


public class RestartAction extends BaseAction {

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();

        ((AppiumDriver) getDriver()).resetApp();

        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail("Application was reset"  );
        return stepResult;
    }
}
