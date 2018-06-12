package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;
import org.apache.log4j.Logger;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;

public class FrameAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(FrameAction.class
			.getName());

	boolean result = false;
	String status = null;
	String stepDetail = null;

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		switch ((String) testData.get("frameaction")) {
		case "switchto":
//			getDriver().switchTo().frame("ifBillMeLaterTermsAndConditions");
			String id = getPageObject().getPropertyByType(getObjectName(), "id");
			if(id == null){
				 WebElement curElement = getWebElement();
				 getDriver().switchTo().frame(curElement);
			}else{
				
				getDriver().switchTo().frame(id);
			}
			
			result = true;
			status = "Passed";
			stepDetail = "Switch to frame successful, frame id: " + id;
			logger.debug("Switch to frame successful, frame id: " + id);
			break;
		case "switchback":
			getDriver().switchTo().defaultContent();
			result = true;
			status = "Passed";
			stepDetail = "Switch back to default content(frame)";
			logger.debug("Switch back to default content(frame");
		
		}
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}

}
