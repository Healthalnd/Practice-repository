package com.qvc.selenium.actions;

import org.openqa.selenium.WebElement;

import java.util.List;

public class StoreElementFromListAction extends BaseAction {
	@Override
	public Object runStoreAction() {
		
		super.logAction();

		List<WebElement> elements = getPageObject().getWebElements(getDriver(),
				getObjectName());

		return elements;
	}
}
