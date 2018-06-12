package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitAction extends BaseAction {


	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		String stepDetail = null;
		int waitTime = 0;

		//if we have set the wait time in the test step, wait * seconds
		Object wait = testData.get("wait");
		if (wait != null) {
			waitTime = Integer.parseInt(wait.toString());
			if(waitTime > 0){//wait time > 0, mean we set the wait time - default was 0
				try {
					Thread.sleep(waitTime * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if(waitTime == 0){
			stepDetail = "Wait for element show up - " + getObjectName();
		}else{
			stepDetail = "Wait " + waitTime + " seconds for element show up - " + getObjectName();					
		}
        // set default timeout
        setTimeout(120000);

        getWebElement();

        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail(stepDetail);
        return stepResult;
	}

}
