package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import org.openqa.selenium.WebElement;

import java.util.HashMap;

public class VerifyTextEqualAction extends BaseAction {

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		String stepDetail;
		String actText;
		String expText;
		
		WebElement object = getWebElement();
		// fail if not found or not displayed
		if (object == null || !object.isDisplayed()) {
			logger.debug("object was null or not displayed, please check the object propertise was corrected - " + getObjectName());;

			stepDetail = "Expect text was not found - because the object can't identify! " ;
			logger.error("Verification Failed. " + stepDetail);

			stepResult.setResult(false);
			stepResult.setStatus("Failed");
			stepResult.setStepDetail(stepDetail);

			return stepResult;
		}

		if (getDriver() instanceof IOSDriver && object.getTagName().equalsIgnoreCase("UIAButton")) {
			actText = getButtonRelatedText((MobileElement) object);
		} else
			actText = object.getText();

		if(actText == null || actText.isEmpty() ){
			actText = object.getAttribute("value");//if getText() was blank, try get value of attribute: "value"
		}
		if(actText == null || actText.isEmpty() ){
			object = getWebElement();
			actText = object.getText();
			if(actText == null || actText.isEmpty() )
				actText = object.getAttribute("value");
		}

		// Text equal compare
		expText = (String) testData.get("text");
		if(actText != null && actText.trim().equals(expText.trim())){
			stepResult.setResult(true);
			stepResult.setStatus("Passed");
			stepDetail = "Expect text was equal to actual text, expect text is: " + expText;
		}else{
			stepResult.setResult(false);
			stepResult.setStatus("Failed");
			stepDetail = "Expect text was not equal to actual text, expect text was: " + expText + ", actul text: " + actText;
			logger.error("Verification Failed. " + stepDetail);
		}

		stepResult.setStepDetail(stepDetail);

		return stepResult;

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
