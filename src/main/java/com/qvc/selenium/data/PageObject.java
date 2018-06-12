package com.qvc.selenium.data;

import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ScrollsTo;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
/*
    In input set checkvisibleattribute = true
     to check visible attribute of element for iOS only
 */

public class PageObject {

    private static Logger logger = Logger.getLogger(PageObject.class);

    public String pageName;
    private long timeout = 5000;
    private HashMap<String, String> lastLocator;

    protected static StoredValueDataManager data = StoredValueDataManager.getInstance();

    protected HashMap<String, Object> testStepData;

    private HashMap<String, ArrayList<HashMap<String, String>>> pageProperties;

    public HashMap<String, ArrayList<HashMap<String, String>>> getProperties() {
        return pageProperties;
    }

    public void setProperties(
            HashMap<String, ArrayList<HashMap<String, String>>> pageProperties) {
        this.pageProperties = pageProperties;
    }

    public PageObject(String pageName) {
        this.pageName = pageName;
    }

    /**
     * 12/15/2014  Add by Wen Zhang(Simon)
     *
     * @param testObjectName
     * @param propertyType
     * @return actual property value
     */
    public String getPropertyByType(String testObjectName, String propertyType) {
        String property = null;
        logger.debug("Called Get property by type(id, name, class...");
        ArrayList<HashMap<String, String>> properties = pageProperties
                .get(testObjectName);
        for (int i = 0; i < properties.size(); i++) {
            HashMap<String, String> curProperty = properties.get(i);
            if (curProperty.containsKey(propertyType)) {
                property = curProperty.get(propertyType);
                logger.debug("return property - " + property);
                break;
            }
        }
        if (property == null) {
            logger.debug("Property value not found by type: " + propertyType + ", in object - " + testObjectName);
        }
        return property;
    }

    public void setTimeout(long pageTimeout) {
        this.timeout = pageTimeout;
    }

    public HashMap<String, Object> getTestStepData() {
        return this.testStepData;
    }

    public void setTestStepData(HashMap<String, Object> testStepData) {
        this.testStepData = testStepData;
    }

    private long getTimeout(String testObjectName, long defaultTimeout) {
        long timeout = defaultTimeout;
        if (testObjectName != null && pageProperties.containsKey(testObjectName)) {
            ArrayList<HashMap<String, String>> propList = pageProperties.get(testObjectName);
            for (HashMap<String, String> hashMap : propList) {
                if (hashMap.containsKey("timeout")) {
                    timeout = Long.parseLong(hashMap.get("timeout")) * 1000;
                    break;
                }
            }
        }
        return timeout;
    }

    /**
     * Tries to find element by specified properties (locators).
     *
     * @param driver      Current driver for automation.
     * @param curProperty Map of properties (locators) for an element.
     * @return Found element.
     * @throws Exception
     */
    public WebElement findElement(RemoteWebDriver driver, HashMap<String, String> curProperty) throws Exception {
        boolean scrollParametr = true;
        if (testStepData != null && testStepData.containsKey("scroll") && "false".equalsIgnoreCase((String)testStepData.get("scroll"))){
            scrollParametr = false;
        } 
        String platform = (String) data.getStoredData("app");
        if(platform.toLowerCase().contains("web")){//when running on Web, no need to scroll - Simon, 12/17/2015
        	scrollParametr = false;
        }
        boolean checkVisibleAttribute = false;
        if (testStepData != null && testStepData.containsKey("checkvisibleattribute") && "true".equalsIgnoreCase((String) testStepData.get("checkvisibleattribute"))) {
            checkVisibleAttribute = true;
        }
        //First of all we try find element by label and scroll to it.
        WebElement curWebElement = null;
        if (scrollParametr)
            curWebElement = scrollToLabel(curProperty, driver, checkVisibleAttribute);

        if (curProperty.containsKey("id")) {
            curWebElement = driver.findElement(By.id(curProperty
                    .get("id")));
        } else if (curProperty.containsKey("name")) {
            curWebElement = driver.findElement(By.name(curProperty
                    .get("name")));
        } else if (curProperty.containsKey("tag")) {
            if (driver instanceof QVCiOSDriver) {
                String tagValue = curProperty.get("tag");
                if (tagValue.substring(0, 1).equals("*") && tagValue.substring(tagValue.length() - 1).equals("*"))
                    tagValue = tagValue.substring(1, tagValue.length() - 1);

                if (scrollParametr)
                    curWebElement = ((QVCiOSDriver) driver).scrollToXPath("//*[@name=\""+tagValue+"\"]", checkVisibleAttribute);
                else
                    curWebElement = ((AppiumDriver) driver).findElementByAccessibilityId(tagValue);
            } else if (scrollParametr && driver instanceof QVCAndroidDriver){
                curWebElement = ((QVCAndroidDriver)driver).scrollToTag(curProperty.get("tag"));
            } else {
                curWebElement = driver.findElement(By.id(curProperty.get("tag")));
            }
        } else if (curProperty.containsKey("htmltag")) {
            curWebElement = driver.findElement(By
                    .tagName(curProperty.get("htmltag")));
        } else if (curProperty.containsKey("class")) {
            curWebElement = driver.findElement(By
                    .className(curProperty.get("class")));
        } else if (curProperty.containsKey("linkText")) {
            curWebElement = driver.findElement(By
                    .linkText(curProperty.get("linkText")));
        } else if (curProperty.containsKey("xpath")) {
            String xpath = curProperty.get("xpath");
            if (scrollParametr && driver instanceof QVCiOSDriver)
                curWebElement = ((QVCiOSDriver)driver).scrollToXPath(xpath, checkVisibleAttribute);
            else if (scrollParametr && driver instanceof QVCAndroidDriver)
                curWebElement = ((QVCAndroidDriver)driver).scrollToXPath(xpath);
            else
                curWebElement = driver.findElementByXPath(xpath);
        } else if (curProperty.containsKey("css")) {
            curWebElement = driver.findElement(By
                    .cssSelector(curProperty.get("css")));
        }

        return curWebElement;
    }

    public WebElement getWebElement(RemoteWebDriver driver, String testObjectName) throws Exception {
        // get element with logging errors
        return getWebElement(driver, testObjectName, true);
    }

    /**
     * Finds an element for specified testObjectName.
     *
     * @param driver         Current driver for automation.
     * @param testObjectName Name of searchable test object.
     * @return Found MobileElement
     */
    public WebElement getWebElement(RemoteWebDriver driver, String testObjectName, Boolean logError) throws Exception {
        WebElement curWebElement = null;
        Exception lastError = null;
        logger.debug("Called Get Web Element");
        ArrayList<HashMap<String, String>> properties = pageProperties
                .get(testObjectName);
        boolean isObjectFound = false;
        if (properties != null) { // add by Simon - to make the properties was load

            lastLocator = null;
            String lastMessage = "";
//            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

            timeout = getTimeout(testObjectName, timeout);
            long end = System.currentTimeMillis() + timeout;
            logger.debug("Timeout: "+timeout);
            while (System.currentTimeMillis() < end) {
                for (int i = 0; i < properties.size(); i++) {
                    isObjectFound = true;
                    HashMap<String, String> curProperty = properties.get(i);
                    curProperty = substituteVar(curProperty);
                    lastLocator = curProperty;
                    try {
                        curWebElement = findElement(driver, curProperty);
                        if(!curWebElement.isDisplayed()){//if the object not displayed, still mark it as not found  - Simon, 6/21/2016
                        	isObjectFound = false;
                        	logger.info("Object not displayed, reload the object - " + testObjectName);
                        }
                    } catch (Exception e) {
                        lastMessage = "Exception: " + e.getMessage();
                        isObjectFound = false;
                        lastError = e;
                    }

                    if (isObjectFound) {
                        break;
                    }
                }
                if (isObjectFound) {
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!isObjectFound && logError) {
                logger.info("Locator was: " + lastLocator);
                if ((lastMessage == null || lastMessage.isEmpty()) && (lastLocator==null || lastLocator.isEmpty()))
                    logger.error("No locator for \"" + testObjectName + "\" in " + pageName);
                else
                    logger.error(lastMessage);
            }
        } else // properties was null - not find the object in the xml page
        // object file
        {
            logger.error(testObjectName + " object not found in " + pageName);
        }
//        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        if (!isObjectFound || curWebElement == null) {
            logger.debug("Get Web Element Object Not Found");
            if (lastError != null)
                throw lastError;
            else
                throw new NoSuchElementException("Element Not Found: "
                        + testObjectName + ". = "
                        + " Please Verify Properties in Object Map are correct.");
        }
        logger.debug("Returning ... " + curWebElement.getText());
        return curWebElement;

    }

    /**
     * Tries to find all elements by specified properties (locators).
     *
     * @param driver      Current driver for automation.
     * @param curProperty Map of properties (locators) for an element.
     * @return Found elements.
     * @throws Exception
     */
    public List<WebElement> findElements(RemoteWebDriver driver, HashMap<String, String> curProperty, boolean checkVisibleAttribute) throws Exception {

        //First of all we try find elements by label and scroll to it.
        List<WebElement> curWebElements = new ArrayList<WebElement>();
        try {
            curWebElements.add(scrollToLabel(curProperty, driver,checkVisibleAttribute ));
        } catch (Exception e) {
            logger.debug(e);
        }

        if (curProperty.containsKey("id")) {
            curWebElements = driver.findElements(By.id(curProperty
                    .get("id")));
        } else if (curProperty.containsKey("name")) {
            curWebElements = driver.findElements(By.name(curProperty
                    .get("name")));
        } else if (curProperty.containsKey("tag")) {
            curWebElements = ((AppiumDriver)driver).findElementsByAccessibilityId(curProperty.get("tag"));
        } else if (curProperty.containsKey("htmltag")) {
            curWebElements = driver.findElements(By.tagName(curProperty
                    .get("htmltag")));
        } else if (curProperty.containsKey("class")) {
            curWebElements = driver.findElements(By
                    .className(curProperty.get("class")));
        } else if (curProperty.containsKey("linkText")) {
            curWebElements = driver.findElements(By
                    .linkText(curProperty.get("linkText")));
        } else if (curProperty.containsKey("xpath")) {
            curWebElements = driver.findElementsByXPath(curProperty.get("xpath"));
        } else if (curProperty.containsKey("css")) {
            curWebElements = driver.findElements(By
                    .cssSelector(curProperty.get("css")));
        }

        return curWebElements;
    }

    public List<WebElement> getWebElements(RemoteWebDriver driver,
                                           String testObjectName) {
        List<WebElement> curWebElements = null;

        ArrayList<HashMap<String, String>> properties = pageProperties
                .get(testObjectName);
        if (properties == null) {
            logger.error(testObjectName + " object not found in " + pageName);
        }
        boolean isObjectFound = false;

        lastLocator = null;
        String lastMessage = "";

        timeout = getTimeout(testObjectName, timeout);
        long end = System.currentTimeMillis() + timeout;

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        while (System.currentTimeMillis() < end) {
            if (properties != null) {
                for (int i = 0; i < properties.size(); i++) {
                    HashMap<String, String> curProperty = properties.get(i);
                    curProperty = substituteVar(curProperty);
                    lastLocator = curProperty;

                    try {
                        curWebElements = findElements(driver, curProperty, false);
                        isObjectFound = true;
                    } catch (NoSuchElementException nse) {
                        lastMessage = "NoSuchElementException: " + nse.getMessage();
                        isObjectFound = false;
                    } catch (Exception e) {
                        lastMessage = "Exception: " + e.getMessage();
                        isObjectFound = false;
                    }

                    if (isObjectFound) {
                        break;
                    }
                }
                if (isObjectFound) {
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!isObjectFound) {
            logger.info("Locator was: " + lastLocator);
            logger.error(lastMessage);
        }
//        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        if (!isObjectFound) {
            throw new NoSuchElementException("Element Not Found: "
                    + testObjectName + ". = "
                    + " Please Verify Properties in Object Map are correct.");
        }

        return curWebElements;

    }

    public HashMap<String, String> getLastLocator(){
        return lastLocator;
    }

    private WebElement scrollToLabel(HashMap<String, String> curProperty, RemoteWebDriver driver, boolean checkVisibileAttribute) throws Exception {
        if (curProperty.containsKey("label") || curProperty.containsKey("name") || curProperty.containsKey("value")) {

            String label = curProperty.get("label");
            if (label == null)
                label = curProperty.get("name");
            if (label == null)
                label = curProperty.get("value");
            if (label.substring(0, 1).equals("*") && label.substring(label.length() - 1).equals("*")) {
                label = label.substring(1, label.length() - 1);
                return ((ScrollsTo) driver).scrollTo(label);
            } else if (driver instanceof QVCiOSDriver){
                return ((QVCiOSDriver) driver).scrollToExact(label, checkVisibileAttribute);
            } else {
                return ((ScrollsTo) driver).scrollToExact(label);
            }

        }
        return null;
    }

    /**
     * Replaces &lt;var_name&gt; in xml file by value of 'var_name' from data file.
     *
     * @param inputData Locators to a control
     * @return Updated locators (with variable values instead of variable names)
     */
    protected HashMap<String, String> substituteVar(HashMap<String, String> inputData) {
        HashMap<String, String> resultData = new HashMap<String, String>();

        for (String key : inputData.keySet()) {
            // if the value is a variable look it up
            String value = inputData.get(key);
            logger.debug("Check if we have a <> in :" + value);
            for(int i = 0; i < 10; i ++){//make sure we replace all <var_name> with actual values
            	if (value.contains("<") && value.contains(">")) {
            		
            		String varName = value.substring(value.indexOf("<") + 1, value.indexOf(">", value.indexOf("<")));
            		String varNameLowerCase = varName.toLowerCase();
            		logger.debug("Parsing Var :" + varName);
            		if (testStepData != null && testStepData.containsKey(varNameLowerCase)) {
            			logger.debug("Getting from step data " + varName);
            			value = value.replace("<" + varName + ">", (CharSequence) testStepData.get(varNameLowerCase));
            		} else {
            			String varValueFromDataFile = (String) data.getStoredData(varNameLowerCase);
            			if (varValueFromDataFile != null) {
            				logger.debug("Getting from test data " + varName);
            				value = value.replace("<" + varName + ">", varValueFromDataFile);
            			}
            		}
            	} else
            		break;
            	
            }
            resultData.put(key, value);
        }
        return resultData;
    }

    public List<HashMap<String, String>> getCustomLocator(String object) {
        if (object != null)
            return pageProperties.get(object);
        else
            return pageProperties.get("test");
    }
}
