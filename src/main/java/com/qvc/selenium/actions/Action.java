package com.qvc.selenium.actions;

import com.qvc.selenium.data.PageObject;
import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;

public interface Action {

	public ExecuteResult runAction() throws Exception;

	public Object runStoreAction();

	public void setPage(PageObject Page);

	public PageObject getPageObject();

	public String getResults();

	public void setObjectName(String objectName);

	public String getObjectName();

	public void setComment(String comment);

	public String getComment();

	public RemoteWebDriver getDriver();

	public void setDriver(RemoteWebDriver driver);

	public void setActionType(String actionType);

	public String getActionType();

	public void setLogSearchControlError(Boolean logSearchControlError);

    public void setStepData(HashMap<String, Object> testStepData);

	public WebElement getWebElement() throws Exception;

}
