package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.WebElement;

public class VerifyStartsWithAction extends BaseAction {

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        boolean result = false;
        String status = null;
        String stepDetail = null;

        String textToStart = (String) testData.get("text");

        WebElement object = getWebElement();
        if (object != null) {
            String objectText = object.getText();

            if (objectText.startsWith(textToStart)) {//not contain
                result = true;
                status = "Passed";
                stepDetail = "Object starts with text: \"" + textToStart + "\"";
            } else {
                result = false;
                status = "Failed";
                stepDetail = "Object with text: \"" + objectText + "\" doesn't start with text: \"" + textToStart + "\"";
            }
        }

        stepResult.setResult(result);
        stepResult.setStatus(status);
        stepResult.setStepDetail(stepDetail);
        return stepResult;

    }
}
