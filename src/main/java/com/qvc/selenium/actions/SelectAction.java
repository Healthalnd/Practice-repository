package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ScrollsTo;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
Select indicated value in a combo box, or dropdown list.
Inputs (like "text=item_to_select"):
    text - a text to fill in the field;
    value - same as 'text';
    hideKeyboardStrategy - strategy to hide keyboard after filling on iOS (doesn't work by default on iOS)
        Possible values: "tapOutside", "swipeDown", "pressKey", "null", "".
        ("null" or empty value means "don't hide keyboard")
 */
public class SelectAction extends BaseAction{
    private String label = null;

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		//use to take case the Select(WebList)
//		new Select(driver.findElement(By.id("shipSelected11"))).selectByVisibleText("UPS Ground @ $6.97");
        WebElement curElement = getWebElement();
        if (curElement == null) {
            logger.debug("Element not found");
            stepResult.setResult(false);
            stepResult.setStatus("Failed");
            stepResult.setStepDetail("Element not found");

            return stepResult;
        }

        String item = (String) testData.get("text");
        if (item == null)
            item = (String) data.getStoredData("text");
        if (item == null)
            item = (String) data.getStoredData("value");
        String actText = null;
        boolean found = false;

        // select on mobile app
        if (getDriver() instanceof AppiumDriver) {
            if (testData.containsKey("label") || getPageObject().getLastLocator().containsKey("label")) {
                label = (String) testData.get("label");
                if (label == null)
                    label = getPageObject().getLastLocator().get("label");
                curElement = getElementByLabel(label);
            }

            curElement.click();
            sleep(1);

            // click on matching item
            if (item.substring(0, 1).equals("*") && item.substring(item.length() - 1).equals("*")) {
                item = item.substring(1, item.length() - 1);
                ((ScrollsTo) getDriver()).scrollTo(item).click();
            } else {
                boolean selected = selectInPicker(item);
                if (!selected)
                    ((ScrollsTo) getDriver()).scrollToExact(item).click();
            }

            actText = getActText();

            stepResult.setResult(true);
            stepResult.setStatus("Passed");
            stepResult.setStepDetail("selected text: " + actText);

            return stepResult;
        }

        // select on Web
        Select webList = new Select(curElement);
        List<WebElement> listItem = webList.getOptions();
        for (WebElement actitem : listItem) {
            actText = actitem.getText();
            if ((actText.toLowerCase()).contains(item.toLowerCase())) {
                webList.selectByVisibleText(actText);
                found = true;
                logger.debug("selected text: " + actText);
                break;
            }
        }

        if (!found) // not fond the text in the weblist
        {
            logger.debug("Text not found on the list: " + item);
            stepResult.setResult(false);
            stepResult.setStatus("Failed");
            stepResult.setStepDetail("Text not found on the list: " + item);
        } else { // find the text and select success
            stepResult.setResult(true);
            stepResult.setStatus("Passed");
            stepResult.setStepDetail("selected text: " + actText);
        }


        return stepResult;
	}

    private WebElement getElementByLabel(String label) throws Exception {
        String xpath = getXPathToText(label);
        xpath += "/parent::*//";

        if (getDriver() instanceof IOSDriver)
            xpath = xpath + "UIAButton[@visible='true']" + "|" + xpath + "UIATextField[@visible='true']";
        else
            xpath = xpath + "android.widget.Spinner" + "|" + xpath + "android.widget.EditText";
        testData.put("xpath", xpath);
        if (testData.containsKey("label"))
            testData.remove("label");
        else {
            HashMap<String, ArrayList<HashMap<String, String>>> pageProperties = getPageObject().getProperties();
            ArrayList<HashMap<String, String>> pageObjectProperties = pageProperties.get(getObjectName());
            HashMap<String, String> pageObjectFirstProperty = new HashMap<String, String>();
            pageObjectFirstProperty.put("xpath", xpath);
            pageObjectProperties.set(0, pageObjectFirstProperty);
            pageProperties.put(getObjectName(), pageObjectProperties);
            getPageObject().setProperties(pageProperties);
        }
        return getWebElement();
    }

    private boolean selectInPicker(String item){
        if (!(getDriver() instanceof IOSDriver))
            return false;

        List<WebElement> foundScrollableParents = getDriver().findElementsByXPath("//UIAPickerWheel[@enabled='true']");
        if (foundScrollableParents.isEmpty())
            return false;

        String[] dateParts = item.split("/");
        if (dateParts.length == 2 || dateParts.length == 3 )
        {
            for (int i = dateParts.length - 1; i >= 0; i--)
            {
                String value = dateParts[i];
                IOSElement foundScrollableParent = (IOSElement) foundScrollableParents.get(i);
                try{
                    foundScrollableParent.sendKeys(value);
                } catch (Exception e){
                    value = (value.substring(0,1).equals("0") ? value.substring(1, value.length()) : value);
                    foundScrollableParent.sendKeys(value);
                }
            }
        } else {
            IOSElement foundScrollableParent = (IOSElement) foundScrollableParents.get(0);
            foundScrollableParent.sendKeys(item);
        }
        IOSElement closeButton = (IOSElement) getDriver().findElementByXPath("//UIAPickerWheel[@enabled='true']/parent::*/preceding-sibling::UIAToolbar[last()]//UIAButton[last()]|"+
                "//UIAPickerWheel[@enabled='true']/parent::*/preceding-sibling::UIAButton[@name=\"Done\"]");
        closeButton.click();
        sleep(3);
        return true;
    }

    private String getActText() throws Exception {
        if (label == null) {
            HashMap<String, Object> pageTestStepData = getPageObject().getTestStepData();
            pageTestStepData.put("scroll", "false");
            getPageObject().setTestStepData(pageTestStepData);
            WebElement curElement = getWebElement();
            if (getDriver() instanceof IOSDriver && curElement.getTagName().equalsIgnoreCase("UIAButton")) {
                String elementName = curElement.getAttribute("name");
                try {
                    return getDriver().findElementByXPath("//*[@name='" + elementName + "']/parent::*//UIATextField").getText();
                } catch (Exception e){
                    logger.debug(e.getMessage());
                    return getDriver().findElementByXPath("//*[@name='" + elementName + "']/parent::*/UIAStaticText[last()]").getText();
                }
            } else
                return curElement.getText();
        }

        String xpath = getXPathToText(label);
        xpath += "/following-sibling::";

        if (getDriver() instanceof IOSDriver)
            xpath = xpath + "UIATextField" + "|" + xpath + "UIAStaticText[last()]";
        else
            xpath = xpath + "android.widget.EditText";
        return getDriver().findElementByXPath(xpath).getText();
    }
}
