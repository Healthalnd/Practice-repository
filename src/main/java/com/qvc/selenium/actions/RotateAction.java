package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.ScreenOrientation;

/*
Rotate screen of a device.
Inputs:
    orientation - orientation of the devise.
        default : "portrait"
        possible values: "landscape", "portrait".
 */
public class RotateAction extends BaseAction {
    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        ScreenOrientation orientation;
        ScreenOrientation currentOrientation;
        String orientationString = (String) testData.get("orientation");
        orientation = "landscape".equalsIgnoreCase(orientationString) ? ScreenOrientation.LANDSCAPE : ScreenOrientation.PORTRAIT;
        currentOrientation = ((AppiumDriver) getDriver()).getOrientation();
        if (!currentOrientation.equals(orientation))
            ((AppiumDriver) getDriver()).rotate(orientation);
        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail("Rotate (orientation=" + orientation + ", currentOrientation=" + currentOrientation + ")");
        return stepResult;
    }
}
