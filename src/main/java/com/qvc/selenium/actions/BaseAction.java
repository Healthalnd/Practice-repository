package com.qvc.selenium.actions;

import com.qvc.selenium.data.PageObject;
import com.qvc.selenium.data.StoredValueDataManager;
import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.ios.IOSDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
Base action class.
Common inputs:
    scroll - If scroll to found element is needed.
        default: "true";
        possible values: "false", anything else.
    checkvisibleattribute - to check visible attribute of element for iOS only
        default: "false"
        possible values: "true", anything else.
 */
public class BaseAction implements Action {

	private PageObject page;
	private RemoteWebDriver driver;
	private String result;
	private String objectName;
	private String comment;
	private String actionType;
	static final Logger logger = Logger.getLogger(BaseAction.class.getName());
	protected static StoredValueDataManager data = StoredValueDataManager
			.getInstance();
    protected HashMap<String, Object> testData;
    protected long timeout = 10000;
    protected ExecuteResult stepResult = new ExecuteResult();
    protected Boolean logSearchControlError = true;

    @Override
	public ExecuteResult runAction() throws Exception {
		// TODO Auto-generated method stub

        return stepResult;
	}

	@Override
	public Object runStoreAction() {
		return null;
	}

	@Override
	public RemoteWebDriver getDriver() {
		return driver;
	}

	@Override
	public void setPage(PageObject pageIn) {
		this.page = pageIn;

	}

	@Override
	public String getResults() {

		return this.result;
	}

	@Override
	public void setObjectName(String inObjectName) {

		this.objectName = inObjectName;
	}

	@Override
	public String getObjectName() {
		return this.objectName;
	}

	@Override
	public void setComment(String inComment) {
		this.comment = inComment;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public PageObject getPageObject() {
		return page;
	}

    @Override
    public void setLogSearchControlError(Boolean logSearchControlError)
    {
        this.logSearchControlError = logSearchControlError;
    }

	@Override
	public void setDriver(RemoteWebDriver inDriver) {
		this.driver = inDriver;
	}

	@Override
	public void setActionType(String actionType) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getActionType() {
		// TODO Auto-generated method stub
		return actionType;
	}

	public void logAction() {
		if (getComment() != null) {
			logger.debug(this.getComment());
		}
	}

    public void setStepData(HashMap<String, Object> testStepData) {
        this.testData = (HashMap<String, Object>) testStepData.clone();
    }

    public void setTimeout(long actionTimeout) {
        this.timeout = actionTimeout;
    }

    private void getTimeoutFromInputs() {
        if (testData != null && testData.containsKey("timeout"))
            setTimeout(Long.parseLong((String) testData.get("timeout"))*1000);
    }
    @Override
    public WebElement getWebElement() throws Exception {
        // get element with logging errors
        return getWebElement(logSearchControlError);
    }

    public WebElement getWebElement(Boolean logError) throws Exception {
        getTimeoutFromInputs();

        WebElement e;
        if (getObjectName() == null || getObjectName().isEmpty()) {
            e = getWebElementWithoutPageObjectFile();
        } else if (getObjectName().equalsIgnoreCase("element")) {
            e = (WebElement) data.getStoredData("element");
        } else {
            page.setTimeout(timeout);
            page.setTestStepData(testData);
            e = page.getWebElement(getDriver(), getObjectName(), logError);

        }
        return e;
    }

    private PageObject getPageObjectFromInputs() {
        page = new PageObject("currentPage");
        HashMap<String, ArrayList<HashMap<String, String>>> prop = new HashMap<String, ArrayList<HashMap<String, String>>>();
        ArrayList<HashMap<String, String>> propData = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> stringTestData = new HashMap<>();
        for (String dataKey : testData.keySet())
            stringTestData.put(dataKey, (String) testData.get(dataKey));
        propData.add(stringTestData);
        prop.put("test", propData);
        page.setProperties(prop);
        page.setTimeout(timeout);
        page.setTestStepData(testData);
        return page;
    }

    private WebElement getWebElementWithoutPageObjectFile() throws Exception {
        page = getPageObjectFromInputs();
        return page.getWebElement(getDriver(), "test");
    }

    public String getXPathToText(String label, String fieldType) {
        String xpath = "//"+fieldType+"[";

        if (label.substring(0, 1).equals("*") && label.substring(label.length() - 1).equals("*")) {
            label = label.substring(1, label.length() - 1);
            if (getDriver() instanceof IOSDriver)
                xpath += "contains(@label,\"" + label + "\") or contains(@value,\"" + label + "\")";
            else
                xpath += "contains(@text,\"" + label + "\")";
        } else {
            if (getDriver() instanceof IOSDriver)
                xpath += "@label=\"" + label + "\"";
            else
                xpath += "@text=\"" + label + "\"";
        }
        xpath += "]";
        return xpath;
    }

    public String getXPathToText(String label) {
        return getXPathToText(label, "*");
    }

    public String getXPathToField(String label, String fieldType) {
        return getXPathToText(label, fieldType) + "|" + getXPathToText(label) + "/following-sibling::" + fieldType;
    }


    public void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<WebElement> getWebElements() {
        getTimeoutFromInputs();

        if (getObjectName() == null) {
            page = getPageObjectFromInputs();
            return page.getWebElements(getDriver(), "test");
        } else {
            page.setTimeout(timeout);
            page.setTestStepData(testData);
            return page.getWebElements(getDriver(), getObjectName());
        }
    }

}
