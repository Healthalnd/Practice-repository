package com.qvc.selenium.actions;

import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;
import com.qvc.selenium.reporting.ExecuteResult;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.List;

/*
Swipe a control.
Inputs:
    direction - direction for the swipe;
    	default value : "up";
        possible values: "up", "down", "left", "right".
    duration - duration (in secs) of the swipe.
        default: 2s;
 */
public class SwipeAction extends BaseAction {
	private Point location;
	private Dimension size;

	private int startx;
	private int starty;
	private int endx;
	private int endy;

	private int offset = 70;
	private int duration = 2000;

	@Override
	public ExecuteResult runAction() throws Exception {
		super.logAction();

		setLocationAndSize();

		String direction = getDirection();

		setSwipeCoordinates(direction);
		setDuration();

		((AppiumDriver) getDriver())
				.swipe(startx, starty, endx, endy, duration);

		stepResult.setResult(true);
		stepResult.setStatus("Passed");
		stepResult.setStepDetail("Swipe " + direction);
		return stepResult;
	}

	private void setLocationAndSize() throws Exception {
		String object = getObjectName();

		location = new Point(0, 0);
		size = new Dimension(0, 0);

		for (int i = 0; i < 10; i++) {
			try {
				if (object != null && !object.isEmpty()) {
					WebElement mobileElement = getWebElement(false);
					location = mobileElement.getLocation();
					size = mobileElement.getSize();
				} else {
					String xpath;
					WebElement window = null;

					if (getDriver() instanceof QVCAndroidDriver) {
						xpath = "//*[@scrollable='true'][1]/parent::*";
						List<WebElement> elements = getDriver().findElementsByXPath(xpath);
						if (!elements.isEmpty())
							window = elements.get(0);
					} else if (getDriver() instanceof QVCiOSDriver) {
						xpath = "//UIAApplication";
						window = getDriver().findElementByXPath(xpath);
					}

					location = window.getLocation();
					size = window.getSize();
				}
			} catch (NoSuchElementException e) {
				continue;
			}
			if (size.width != 0 && size.height != 0)
				break;
		}
		if (size.width == 0)
			throw new RuntimeException("Control size was not obtained.");

	}

	private String getDirection() {
		String direction = "up";
		if (testData != null && testData.containsKey("direction"))
			direction = (String) testData.get("direction");
		return direction;
	}

	private void setDuration() {
		if (testData != null && testData.containsKey("duration"))
			duration = Integer.parseInt((String) testData.get("duration")) * 1000;
	}

	private void setSwipeCoordinates(String direction) {

		switch (direction.toLowerCase()) {
		case "left":
			startx = location.x + size.width - offset;
			starty = location.y + size.height / 2;
			endx = location.x + offset;
			endy = location.y + size.height / 2;
			break;
		case "right":
			startx = location.x + offset;
			starty = location.y + size.height / 2;
			endx = location.x + size.width - offset;
			endy = location.y + size.height / 2;
			break;
		case "down":
			startx = location.x + size.width / 2;
			starty = location.y + offset;
			endx = location.x + size.width / 2;
			endy = location.y + size.height - offset;
			break;

		default:
			startx = location.x + size.width / 2;
			starty = location.y + size.height - offset;
			endx = location.x + size.width / 2;
			endy = location.y + offset;
			break;
		}

	}
}
