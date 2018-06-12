package com.qvc.selenium.drivers;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Response;
//import org.openqa.selenium.remote.SessionNotFoundException;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.google.common.base.Throwables;

import io.appium.java_client.MobileElement;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.*;

/**
 * Extension for iOSDriver with correct scrolling.
 */
public class QVCiOSDriver extends IOSDriver {
	static final Logger logger = Logger.getLogger(QVCiOSDriver.class
			.getName());

	public QVCiOSDriver(URL remoteAddress, Capabilities desiredCapabilities) {
		super(remoteAddress, desiredCapabilities);
	}

	/**
	 * Scrolls to an element with the text as part of 'text' or 'description' attribute.
	 *
	 * @param text Substring for a control 'text' or 'description'.
	 * @return MobileElement with specified text.
	 */
	@Override
	public MobileElement scrollTo(String text) {
		IOSElement foundTableCell;
		for (String attribute : Arrays.asList("label", "value", "name")){
			try {
				foundTableCell = (IOSElement) findElementByXPath("//*[contains(@"+attribute+",\"" + text + "\")]/ancestor-or-self::UIATableCell|//*[contains(@"+attribute+",\"" + text + "\")]/ancestor-or-self::UIATableView/*[contains(@"+attribute+",\"" + text + "\")]");
				foundTableCell.findElementByIosUIAutomation(".scrollToVisible()");
				return (MobileElement) findElementByXPath("//*[contains(@"+attribute+",\"" + text + "\")]");
			} catch (Exception e) {
				logger.debug("Scrolling was failed. " + e.getMessage());
			}
		}
		return (MobileElement) findElementByXPath("//*[contains(@label,\"" + text + "\") or contains(@value,\"" + text + "\") or contains(@name,\"" + text + "\")]");
	}

	public MobileElement scrollTo(String text, String attribute) {
		try {
			IOSElement foundTableCell = (IOSElement) findElementByXPath("//*[contains(@" + attribute + ",\"" + text + "\")]/ancestor-or-self::UIATableCell");
			foundTableCell.findElementByIosUIAutomation(".scrollToVisible()");
		} catch (Exception e) {
			logger.debug("Scrolling was failed. " + e.getMessage());
		}
		return (MobileElement) findElementByXPath("//*[contains(@"+attribute+",\""+text+"\")]");
	}

	/**
	 * Scrolls to an element which 'text' or 'description' attribute equals specified text.
	 *
	 * @param text String value for a control 'text' or 'description'.
	 * @return MobileElement with specified text.
	 */
	@Override
	public MobileElement scrollToExact(String text) {
		for (String attribute : Arrays.asList("label", "value", "name")){
			try {
				IOSElement foundTableCell = (IOSElement) findElementByXPath(
						"//UIATableView[@visible='true']//*[@"+attribute+"=\"" + text + "\"]/ancestor-or-self::UIATableCell|"+
						"//*[@"+attribute+"=\"" + text + "\"]/ancestor-or-self::UIATableView[@visible='true']/*[@"+attribute+"=\"" + text + "\"]");
				foundTableCell.findElementByIosUIAutomation(".scrollToVisible()");
				return (MobileElement) findElementByXPath("//*[@"+attribute+"=\""+text+"\"]");
			} catch (Exception e) {
				logger.debug("Scrolling was failed. " + e.getMessage());
			}
		}
		return (MobileElement) findElementByXPath("//*[@label=\""+text+"\" or @value=\""+text+"\" or @name=\""+text+"\"]");
	}

    public MobileElement scrollToExact(String text, boolean checkVisible) {
        MobileElement element = scrollToExact(text);

        if (checkVisible) {
            return (MobileElement) findElementByXPath("//*[@label=\"" + text + "\" or @value=\"" + text + "\" or @name=\"" + text + "\"][@visible='true']");
        } else
            return element;
    }

	public MobileElement scrollToExact(String text, String attribute) {
		try {
			IOSElement foundTableCell = (IOSElement) findElementByXPath(
					"//UIATableView[@visible='true']//*[@" + attribute + "=\"" + text + "\"]/ancestor-or-self::UIATableCell");
			foundTableCell.findElementByIosUIAutomation(".scrollToVisible()");
		} catch (Exception e) {
			logger.debug("Scrolling was failed. " + e.getMessage());
		}
		return (MobileElement) findElementByXPath("//*[@"+attribute+"=\""+text+"\"]");
	}

	/**
	 * Scrolls to an element with specified xpath.
	 *
	 * @param xpath XPath to a control, which should become visible.
	 * @return Found MobileElement.
	 */
	public MobileElement scrollToXPath(String xpath, boolean checkVisibleAttribute){
		// check if element is present
		IOSElement foundElement = (IOSElement) findElementByXPath(xpath);

		// get scrollable parent
		String parentXPath = "";
		for ( String xpathPart : xpath.split("\\|")){
			parentXPath += "//UIATableView/*[."+xpathPart+"]|";
            parentXPath += "//UIATableView"+xpathPart+"|";
            parentXPath += "//UIACollectionView/*[."+xpathPart+"]|";
            parentXPath += "//UIACollectionView"+xpathPart+"|";
		}
		parentXPath = parentXPath.substring(0, parentXPath.length()-1);

		IOSElement foundScrollableParent = null;
		try {
			foundScrollableParent = (IOSElement) findElementByXPath(parentXPath);
		} catch (Exception e){
			logger.debug("Parent scrollable was not found for " + xpath);
		}

		if (foundScrollableParent != null) {
			// scroll to make visible if element is present
			foundScrollableParent.findElementsByIosUIAutomation(".scrollToVisible()");
			//scrollToMakeVisible(foundElement);
		}
        if (checkVisibleAttribute ){
            if (foundElement.isDisplayed())
                return foundElement;
            else return null;
        }
        return foundElement;

	}

	/**
	 * Scrolls parent page to make specified element visible on the screen.
	 *
	 * @param foundElement WebElement, which should become visible.
	 * @return Found MobileElement.
	 */
	private WebElement scrollToMakeVisible(IOSElement foundElement) {

		((IOSElement) foundElement.findElementByXPath("/ancestor-or-self::UIATableCell")).findElementsByIosUIAutomation(".scrollToVisible()");

		return foundElement;
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void zoom(int x, int y) {
		MultiTouchAction multiTouch = new MultiTouchAction(this);

		int scrHeight = manage().window().getSize().getHeight();
		int yOffset = 100;

		if (y - 100 < 0) {
			yOffset = y;
		} else if (y + 100 > scrHeight) {
			yOffset = scrHeight - y;
		}

		TouchAction action0 = new TouchAction(this).press(x, y)
				.moveTo(x,  -yOffset).release();
		TouchAction action1 = new TouchAction(this).press(x, y)
				.moveTo(x, yOffset).release();

		multiTouch.add(action0).add(action1);

		multiTouch.perform();
	}

    @Override
	public Response execute(String driverCommand, Map<String, ?> parameters) {
		Command command = new Command(getSessionId(), driverCommand, parameters);
		Response response;

		long start = System.currentTimeMillis();
		String currentName = Thread.currentThread().getName();
		Thread.currentThread().setName(
				String.format("Forwarding %s on session %s to remote",
						driverCommand, getSessionId()));
		try {
			log(getSessionId(), command.getName(), command, When.BEFORE);
			response = getCommandExecutor().execute(command);
			log(getSessionId(), command.getName(), command, When.AFTER);

			if (response == null) {
				return null;
			}

			// Unwrap the response value by converting any JSON objects of the
			// form
			// {"ELEMENT": id} to RemoteWebElements.
			Object value = getElementConverter().apply(response.getValue());
			response.setValue(value);
		/*} catch (SessionNotFoundException e) {
			throw e;*/
		} catch (Exception e) {
			log(getSessionId(), command.getName(), command, When.EXCEPTION);
			String errorMessage = "Error communicating with the remote browser. "
					+ "It may have died.";
			if (driverCommand.equals(DriverCommand.NEW_SESSION)) {
				errorMessage = "Could not start a new session. Possible causes are "
						+ "invalid address of the remote server or browser start-up failure.";
			}
			throw new UnreachableBrowserException(errorMessage, e);
		} finally {
			Thread.currentThread().setName(currentName);
		}

		try {
			getErrorHandler().throwIfResponseFailed(response,
					System.currentTimeMillis() - start);
		} catch (WebDriverException ex) {
			ex.addInfo(WebDriverException.DRIVER_INFO, this.getClass()
					.getName());
			if (getSessionId() != null) {
				ex.addInfo(WebDriverException.SESSION_ID, getSessionId()
						.toString());
			}
			if (getCapabilities() != null) {
				ex.addInfo("Capabilities", getCapabilities().toString());
			}
			try {
				Map<String, Object> rawErrorData = (Map<String, Object>) response.getValue();
				if (rawErrorData.containsKey("origValue"))
					ex.addInfo("Original message",
							(String) rawErrorData.get("origValue") + "\n");
			} catch (Exception e) {
				;
			}

			Throwables.propagate(ex);
		}
		return response;
	}

}
