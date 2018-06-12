package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;

import java.util.List;

import org.openqa.selenium.WebElement;

public class VerifyElementsCount extends BaseAction {

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        boolean result = false;
        String status = null;
        String stepDetail = null;

        String expCount = (String) testData.get("count");
        List<WebElement> elms = getPageObject().getWebElements(getDriver(),this.getObjectName());
        String actCount = Integer.toString(elms.size());
        
        if (expCount.equals(actCount)) {
            result = true;
            status = "Passed";
            stepDetail = "Count is " + actCount ;
         } else {
            result = false;
            status = "Failed";
            stepDetail = "exp count: " + expCount + ", but act is: " + actCount;
            
        }

        stepResult.setResult(result);
        stepResult.setStatus(status);
        stepResult.setStepDetail(stepDetail);
        return stepResult;

    }
}
