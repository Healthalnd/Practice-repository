package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

public class ZoomAction extends BaseAction{
	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();
		Point center = new Point(0, 0);
		if (getObjectName() != null){
			
			for (int i=0; i< 10; i++){
				try{
					center = ((MobileElement) getWebElement()).getCenter();
				}
				catch (NoSuchElementException e){
					continue;
				}
				if (center.y != 0 && center.x != 0)
					break;
			}
			if (center.y == 0)
				throw new RuntimeException("Location was not found.");
			
		}
		else{
			Dimension size = ((AppiumDriver)getDriver()).manage().window().getSize();
			center = new Point(size.width/2, size.height/2);
		}
		
		((AppiumDriver)getDriver()).zoom(center.x, center.y);
		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Zoom from: x=" + center.x + ", y=" + center.y);
		return stepResult;
	}
}
