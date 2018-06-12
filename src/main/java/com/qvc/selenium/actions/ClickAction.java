package com.qvc.selenium.actions;

import com.qvc.selenium.drivers.QVCiOSDriver;
import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/*
Click/tap the control.
Inputs:

    duration - time (in secs) of the action
                default value : 0;

    actionBuilder - type of click when we use base WebDriver Action Builder for clock perform
        use it when current clock method do not work.
                default: 'false'
                possible: 'true' or anything else
                note: 'duration' parameter not used for this actionType

 */
public class ClickAction extends BaseAction {

    private int getDuration() {
        if (testData != null && testData.containsKey("duration"))
            return Integer.parseInt((String) testData.get("duration")) * 1000;
        else return 0;
    }

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        ExecuteResult stepResult = new ExecuteResult();
        try {
            WebElement curElement = getWebElement();
            if (getDriver() instanceof QVCiOSDriver) {
                try {
                    click(curElement);
                } catch (Exception e) {
                    Point center = ((MobileElement) curElement).getCenter();
                    if (center.getY() <= 0) {
                        sleep(1);
                        center = ((MobileElement) getWebElement()).getCenter();
                    }
                    if (center.getY() > 0) {
                        ((QVCiOSDriver) getDriver()).tap(1, center.getX(), center.getY() - 5, getDuration());
                    }
                }
            } else {
                try {
                    curElement.click();
                } catch (TimeoutException e) {
                    //catch the exception, so not make the test fail,
                    //then can use other method to take care the page load issue
                    logger.debug("ignore the page load timeout issue for click(): " + e.getMessage());
                }
            }
            stepResult.setResult(true);
            stepResult.setStatus("Passed");
            stepResult.setStepDetail("Click Element: " + getObjectName());
        } catch (UnhandledAlertException UAE) {
            throw UAE;
        }
        return stepResult;

    }

    private void click(WebElement curElement) {
        if ("true".equalsIgnoreCase(testData.containsKey("actionBuilder") + "")) {
            new Actions(getDriver()).moveToElement(curElement).click().perform();
        } else {
            ((AppiumDriver) getDriver()).tap(1, curElement, getDuration());
        }
    }

}
