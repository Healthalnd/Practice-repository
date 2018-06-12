package com.qvc.selenium.actions;

import com.qvc.selenium.drivers.PopupWindowReader;
import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;
import com.qvc.selenium.reporting.ExecuteResult;
import com.qvc.selenium.steps.LoopTestStep;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;

import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.HashMap;

/*
Fill a text field by specified value.
Inputs (like "text=value"):
    text - a text to fill in the field;
    input - same as 'text';
    hideKeyboardStrategy - strategy to hide keyboard after filling on iOS (doesn't work by default on iOS)
        Possible values: "tapOutside", "swipeDown", "pressKey", "null", "".
        ("null" or empty value means "don't hide keyboard")
    shell - (Android only) If adb should be used directly for sendKeysAction.
        default value : "false";
        possible values: "true", "false".
    click - If click on edit field is needed before sending keys.
        default: "true";
        possible values: "false", anything else.
 */
public class SendKeysAction extends BaseAction {
    static final Logger logger = Logger.getLogger(LoopTestStep.class.getName());

    @Override
    public ExecuteResult runAction() throws Exception {
        stepResult = new ExecuteResult();
        String inputValue = (String) testData.get("text");
        if (inputValue == null)
            inputValue = (String) testData.get("input");
        logger.debug("in send keys for " + inputValue);
        WebElement we = getWebElement();
        //add switch code to control running for Web or for Mobile, Simon 11/6/2015
        String platform = (String) data.getStoredData("app");
        if(platform.toLowerCase().contains("web"))
        	platform = "web";
        
        switch (platform) {
		case "web": //for Web
			if(inputValue.startsWith("Keys.")){
	        	switch (inputValue){
	        	case "Keys.BACK_SPACE":
	        		we.sendKeys(Keys.BACK_SPACE);
	        		break;
	        	case "Keys.TAB":
	        		we.sendKeys(Keys.TAB);
	        		break;
	        	case "Keys.UP":
	        		we.sendKeys(Keys.UP);
	        		break;
	        	case "Keys.DOWN":
	        		we.sendKeys(Keys.DOWN);
	        		break;
	        	case "Keys.LEFT":
	        		we.sendKeys(Keys.LEFT);
	        		break;
	        	case "Keys.RIGHT":
	        		we.sendKeys(Keys.RIGHT);
	        		break;
	        	case "Keys.ENTER":
	        		we.sendKeys(Keys.ENTER);
	        		break;
	        	case "Keys.SPACE":
	        		we.sendKeys(Keys.SPACE);
	        		break;
	        	case "Keys.ESCAPE":
	        		we.sendKeys(Keys.ESCAPE);
	        		break;
	        	case "Keys.F5":
	        		we.sendKeys(Keys.F5);
	        		break;
	        		
	        	default:
	        		logger.debug("The key value you enter is wrong: " + inputValue+". Please check it again. Or add your key value in code.");
	        		break;
	        		
	        	}
	        }
			else{
				we.clear();
				we.sendKeys(inputValue);
			}
			break;
			
		default: //for Mobile
		
	        if (getPageObject().getLastLocator().containsKey("label")) {
	            String label = (String) testData.get("label");
	            if (label == null)
	                label = getPageObject().getLastLocator().get("label");
	            we = getElementByLabel(label);
	        }
	
	        // Use Text Field instead of found Table Cell
	        if (we instanceof IOSElement && ((IOSElement) we).getTagName().equalsIgnoreCase("UIATableCell"))
	            we = getDriver().findElementByXPath("//*[@name='" + ((IOSElement) we).getAttribute("name") + "']//UIATextField");
	
	        tryClick(we);
	
	        // Clear field if needed
	        if (inputValue.startsWith("\\b")) {
	            inputValue = inputValue.substring(2);
	            try {
	                we.clear();
	            } catch (Exception ne) {
	                logger.debug(ne.getMessage());
	            }
	        }
	
	        // Press Enter after filling if needed
	        boolean pressEnter = false;
	        if (inputValue.endsWith("\\n")) {
	            pressEnter = true;
	            inputValue = inputValue.substring(0, inputValue.length() - 2);
	        }
	
	        // Fill value, update to use setValue method for iOS, it's faster than sendKeys - Simon 11/10/2016
	        try {
	            if ("true".equalsIgnoreCase((String) testData.get("shell"))) {
	                new Actions(getDriver()).moveToElement(we).click().perform();//Focuse in TextEdit
	                PopupWindowReader.sendKeys(inputValue);//shell command for TextInput
	            } else if("false".equalsIgnoreCase((String) testData.get("setvalue"))) {
	            	//setvalue was false, that mean we will use sendKeys method
	                we.sendKeys(inputValue);
	            } else {
	            	//setValue for iOS, sendKeys for Android - Simon 2/20/2017
	            	if (getDriver() instanceof QVCiOSDriver)
	            		((MobileElement)we).setValue(inputValue); //setValue only works for iOS
	            	else
	            		 we.sendKeys(inputValue);//sendkeys for Android
	            }
	        } catch (Exception e) {
	            tryClick(we);
	            //setValue for iOS, sendKeys for Android - Simon 2/20/2017
	            if (getDriver() instanceof QVCiOSDriver)
            		((MobileElement)we).setValue(inputValue); //setValue only works for iOS
            	else
            		 we.sendKeys(inputValue);//sendkeys for Android
	        }
	
	        if (pressEnter)
	            sendEnterKey(we);
	        else {
	            sleep(1);
	            hideKeyboard();
	        }
        }//end switch
        
        
        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail("Input keywords: " + inputValue);
        return stepResult;
    }

    private void tryClick(WebElement we) {
        if ("false".equalsIgnoreCase((String) testData.get("click")))
            return;

        logger.debug("tryClick ==Start==");
        try {
            we.click();
            return;
        } catch (Exception ce) {
            logger.debug(ce.getMessage());
        }

        try {
            Point center = ((MobileElement) getWebElement()).getCenter();
            if (center.getY() > 0) {
                try {
                    ((QVCiOSDriver) getDriver()).tap(1, center.getX(), center.getY() - 3, 0);

                } catch (Exception te) {
                    logger.debug(te.getMessage());
                }
            }
        } catch (Exception te) {
            logger.debug(te.getMessage());
        }
    }

    private void hideKeyboard() {
        String hideKeyboardStrategy = null;

        // hide by default on Android
        if (getDriver() instanceof QVCAndroidDriver) {
            hideKeyboardStrategy = "pressKey";
        }

        // get hide keyboard strategy
        if (testData.containsKey("hidekeyboardstrategy")) {
            hideKeyboardStrategy = (String) testData.get("hidekeyboardstrategy");
            if (hideKeyboardStrategy != null && hideKeyboardStrategy.equals("null") || hideKeyboardStrategy.isEmpty())
                hideKeyboardStrategy = null;
        }

        if (hideKeyboardStrategy != null) {
            // hide keyboard
            try {
                if (getDriver() instanceof QVCAndroidDriver) {
                    ((AppiumDriver) getDriver()).hideKeyboard();
                } else if (getDriver() instanceof QVCiOSDriver) {
                    ((QVCiOSDriver) getDriver()).hideKeyboard(hideKeyboardStrategy, null);
                }
                logger.debug("keyboard hidden");
            } catch (Exception e) {
                logger.debug("keyboard doesn't exist");
            }
        }
    }

    private void sendEnterKey(WebElement we) {
        if (getDriver() instanceof QVCiOSDriver)
            we.sendKeys("\n");
        else if ((getDriver() instanceof QVCAndroidDriver))
            ((QVCAndroidDriver) getDriver()).sendKeyEvent(AndroidKeyCode.ENTER);
        else
            we.sendKeys(Keys.RETURN);
    }

    private WebElement getElementByLabel(String label) throws Exception {
        // Search Edit field if label specified
        String xpath;

        if (getDriver() instanceof IOSDriver)
            xpath = getXPathToText(label, "UIATextField").replace("@label", "@value") + "|" +
                    getXPathToText(label, "UIASecureTextField").replace("@label", "@value") + "|" +
                    getXPathToField(label, "UIATextField") + "|" +
                    getXPathToField(label, "UIASecureTextField") + "|" +
//                    getXPathToField(label, "UIASecureTextField") + "/UIASecureTextField" + "|" +
                    getXPathToField(label, "UIASearchBar");
        else {
            xpath = getXPathToField(label, "android.widget.EditText");
        }
        if (testData.containsKey("label")) {
            testData.remove("label");
            testData.put("xpath", xpath);
        } else {
            HashMap<String, ArrayList<HashMap<String, String>>> pageProperties = getPageObject().getProperties();
            ArrayList<HashMap<String, String>> pageObjectProperties = pageProperties.get(getObjectName());
            HashMap<String, String> pageObjectFirstProperty = new HashMap<String, String>();
            pageObjectFirstProperty.put("xpath", xpath);
            pageObjectProperties.set(0, pageObjectFirstProperty);
            pageProperties.put(getObjectName(), pageObjectProperties);
            getPageObject().setProperties(pageProperties);

            // do not scroll if label found
            HashMap<String, Object> pageTestStepData = getPageObject().getTestStepData();
            pageTestStepData.put("scroll", "false");
            getPageObject().setTestStepData(pageTestStepData);
        }

        return getWebElement();
    }
}
