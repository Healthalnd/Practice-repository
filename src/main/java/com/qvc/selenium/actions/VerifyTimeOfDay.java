package com.qvc.selenium.actions;

import java.util.Calendar;
import java.util.HashMap;
import org.openqa.selenium.WebElement;
import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

/*
 * According to get the current time to judge if it is morning or afternoon or evening.
 */

public class VerifyTimeOfDay extends BaseAction{
	
	@Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
//        boolean result = false;
//        String status = null;
        String stepDetail = null;
        String actGreeting;
        String expGreeting;
       
        WebElement object = getWebElement();
		// fail if not found or not displayed
		if (object == null || !object.isDisplayed()) {
			logger.debug("object was null or not displayed, please check the object properties was corrected - " + getObjectName());;

			stepDetail = "Expect text was not found - because the object can't identify! " ;
			logger.error("Verification Failed. " + stepDetail);

			stepResult.setResult(false);
			stepResult.setStatus("Failed");
			stepResult.setStepDetail(stepDetail);

			return stepResult;
		}

		if (getDriver() instanceof IOSDriver && object.getTagName().equalsIgnoreCase("UIAButton")) {
			actGreeting = getButtonRelatedText((MobileElement) object);
		} else
			actGreeting = object.getText();

		if(actGreeting == null || actGreeting.isEmpty() ){
			actGreeting = object.getAttribute("value");//if getText() was blank, try get value of attribute: "value"
		}
		if(actGreeting == null || actGreeting.isEmpty() ){
			object = getWebElement();
			actGreeting = object.getText();
			if(actGreeting == null || actGreeting.isEmpty() )
				actGreeting = object.getAttribute("value");
		}
		
		// compare
		expGreeting = getTimeOfDay();
		if(actGreeting != null && actGreeting.toLowerCase().contains(expGreeting.toLowerCase())){
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
			stepDetail = "Expect greeting was found: " + expGreeting;
		}else{
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
			stepDetail = "Expect greeting was not found: " + expGreeting + ", actual greeting: " + actGreeting;
			logger.error("Verification Failed. " + stepDetail);
		}

		stepResult.setStepDetail(stepDetail);

		return stepResult;		
    }
	
	//get current time
	private String getTimeOfDay(){
		String greeting = null;
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		if(timeOfDay >= 0 && timeOfDay < 12){
			greeting = "Good Morning";
		}else if(timeOfDay >= 12 && timeOfDay < 17){
			greeting = "Good Afternoon";
		}else if(timeOfDay >= 17 && timeOfDay < 24){
			greeting = "Good Evening";
		}
		return greeting;
	}
	
	private String getButtonRelatedText(MobileElement object){
		HashMap<String, String> lastlocator = getPageObject().getLastLocator();
		String xpath = lastlocator.get("xpath");
		if (xpath !=null && xpath.contains("|")){
			String newXPath = "";
			for (String xpathPart : xpath.split("\\|"))
				if (!xpathPart.contains("UIAButton")) {
					newXPath += "|" + xpathPart.replace("[@visible='true']", "");
				}
			xpath = newXPath.substring(1);
		} else
			xpath = "//*[@name='" + object.getAttribute("name") + "']/parent::*//UIATextField";

		return getDriver().findElementByXPath(xpath).getText();
	}
}
