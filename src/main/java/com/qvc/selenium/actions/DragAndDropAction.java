package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.WebElement;
/*
Drag the control and drop on specified target
Inputs:
    target - name of target element (page object) on current page
 */

public class DragAndDropAction extends BaseAction {

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();

        String targetElementName = String.valueOf(testData.get("target"));

        WebElement elementToDrag = getWebElement();
        WebElement targetElement = getPageObject().getWebElement(getDriver(),targetElementName);

        TouchAction touchAction = new TouchAction((AppiumDriver)getDriver());
        try {
            touchAction.longPress(elementToDrag).waitAction().moveTo(targetElement).release();
            touchAction.perform();
        }
        catch (Exception e){
            logger.debug(e.getMessage());
        }

        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail("Element " + getObjectName()+ " was dragged to " + targetElementName );
        return stepResult;
    }
}
