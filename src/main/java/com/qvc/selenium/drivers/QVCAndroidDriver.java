package com.qvc.selenium.drivers;

import com.google.common.base.Throwables;
import io.appium.java_client.AppiumSetting;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.qvc.selenium.drivers.PopupWindowReader.*;

/**
 * Extension for AndroidDriver with correct scrolling.
 */
public class QVCAndroidDriver extends AndroidDriver {
    private static final Logger logger = Logger.getLogger(QVCAndroidDriver.class.getName());

    public QVCAndroidDriver(URL remoteAddress, Capabilities desiredCapabilities) {
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
        String xpath = "//*[contains(@text,'" + text + "') or contains(@description,'" + text + "') or contains(@content-desc,'" + text + "')]";
        return scrollToXPath(xpath);
    }

    /**
     * Scrolls to an element which 'text' or 'description' attribute equals specified text.
     *
     * @param text String value for a control 'text' or 'description'.
     * @return MobileElement with specified text.
     */
    @Override
    public MobileElement scrollToExact(String text) {
        String xpath = "//*[@text='" + text + "' or @description='" + text + "' or @content-desc='" + text + "']";
        return scrollToXPath(xpath);
    }

    @Override
    public WebElement findElementByXPath(String xpath) {
        if (xpath.contains("//Window["))//For source-invisible popup Elements
            return new QVCCustomMobileElement(this, xpath).find();
        else
            return super.findElementByXPath(xpath);
    }

    @Override
    public List<WebElement> findElementsByXPath(String xpath) {
        if (xpath.contains("//Window["))//For source-invisible popup Elements
        {
            List<WebElement> list = new ArrayList<>();
            QVCCustomMobileElement we = new QVCCustomMobileElement(this, xpath).find();
            if (we != null) {
                list.add(we);
            }
            return list;
        } else
            return super.findElementsByXPath(xpath);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Scrolls to an element with specified xpath.
     *
     * @param xpath XPath to a control, which should become visible.
     * @return Found MobileElement.
     */
    public MobileElement scrollToXPath(String xpath) {
        List<WebElement> foundElements;
        String parentXPath;

        // check if element is present
        foundElements = findElementsByXPath(xpath);

        // get scrollable parent
        if (foundElements.isEmpty()) {
            parentXPath = "//*[@scrollable='true']";
        } else {
            return (MobileElement) foundElements.get(0);
        }
        List<WebElement> foundScrollableParents = findElementsByXPath(parentXPath);

        // Find the element and scroll to make clickable
        for (int i = 0; i < foundScrollableParents.size(); i++) {
            AndroidElement foundScrollableParent = (AndroidElement) foundScrollableParents.get(0);

            String tagName = foundScrollableParent.getTagName();
            logger.debug("Scrollable tag name: " + tagName);

            // skip Spinners
            if (tagName.equalsIgnoreCase("android.widget.Spinner"))
                continue;

            // get coordinates of scrollable parent
            Point parentLocation = foundScrollableParent.getLocation();
            int parentTop = parentLocation.y;
            int parentLeft = parentLocation.x;
            Dimension parentSize = foundScrollableParent.getSize();
            int parentHeight = parentSize.getHeight();
            int parentWidth = parentSize.getWidth();

            // check if element is present
            if (foundElements.isEmpty())
                foundElements = findElementsByXPath(xpath);
            // scroll to make visible if element is present
            if (!foundElements.isEmpty())
                return (MobileElement) scrollToMakeVisible(foundElements.get(0), parentLeft, parentTop, parentWidth, parentHeight);

            // scroll to top
            String prevSource = getPageSource();
            String source;
            for (int swipe_i = 0; swipe_i < 50; swipe_i++) {
                try {
                    swipe(parentLeft + parentWidth - 2, parentTop + 10, parentLeft + parentWidth - 2, parentTop + parentHeight - 10, 0);
//                    sleep(1000);

                } catch (Exception e) {
                    break;
                }

                // break if scrolling doesn't work
                source = getPageSource();
                if (source.equals(prevSource))
                    break;
                prevSource = source;

            }

            // scroll to the element
            prevSource = getPageSource();

            for (int swipe_i = 0; swipe_i < 50; swipe_i++) {
                foundElements = findElementsByXPath(xpath);
                if (!foundElements.isEmpty())
                    return (MobileElement) scrollToMakeVisible(foundElements.get(0), parentLeft, parentTop, parentWidth, parentHeight);

                try {
//                    swipe(parentLeft + parentWidth / 2, parentTop + parentHeight - 3, parentLeft + parentWidth / 2, parentTop + 3, 0);
                    swipe(parentLeft + parentWidth - 2, parentTop + parentHeight - 3, parentLeft + parentWidth - 2, parentTop + 3, parentHeight * 5);
                } catch (Exception e) {
                    break;
                }

//                sleep(500);

                // break if scrolling doesn't work
                source = getPageSource();
                if (source.equals(prevSource))
                    break;
                prevSource = source;
            }
        }

        if (!foundElements.isEmpty())
            return (MobileElement) foundElements.get(0);
        else
            return (MobileElement) findElementByXPath(xpath);
    }

    public WebElement scrollToTag(String tag) {
        return scrollToXPath("//*[@resource-id='" + tag + "']");
    }

    /**
     * Scrolls parent page to make specified element visible on the screen.
     *
     * @param foundElement WebElement, which should become visible.
     * @return Found MobileElement.
     */
    private WebElement scrollToMakeVisible(WebElement foundElement, int parentLeft, int parentTop, int parentWidth, int parentHeight) {
        int top = foundElement.getLocation().y;
        if (top < parentTop)
            swipe(parentLeft + parentWidth / 2, parentTop + 3, parentLeft + parentWidth / 2, parentTop + 3 + (parentTop - top), (parentTop - top) * 20);
        int bottom = foundElement.getLocation().y + foundElement.getSize().height;
        if (bottom > (parentTop + parentHeight))
            swipe(parentLeft + parentWidth / 2, parentTop + parentHeight - 3, parentLeft + parentWidth / 2, bottom - 3, (parentTop + parentHeight - bottom) * 20);
        return foundElement;
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
//        } catch (SessionNotFoundException e) {
//            throw e;
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
            }

            Throwables.propagate(ex);
        }
        return response;
    }

    @Override
    public void ignoreUnimportantViews(Boolean compress) {
        setSetting(AppiumSetting.IGNORE_UNIMPORTANT_VIEWS, false);
    }

    public static void restartEmulator(String emulatorName) {
        try {
            logger.info("Restarting emulator " + emulatorName);
            String closeEmulatorResult = cmd("taskkill /F /IM emulator-x86.exe");
            if (closeEmulatorResult.contains("SUCCESS")){
                logger.debug("Emulator has closed. Info: " + closeEmulatorResult);
            } else{
                logger.debug("Closing emulator error. Info: " + closeEmulatorResult);
            }
            Runtime.getRuntime().exec("emulator -avd " + emulatorName + " -netfast -gpu on");
            logger.debug("waiting for emulator full booted");
            int maxWaiting = 10 * 60 * 1000; //10 minutes
            do {
                String res = cmd("adb shell getprop init.svc.bootanim");
                logger.debug("process return " + res);
                if (res.contains("stopped")){
                    logger.info("Emulator has booted");
                    return;
                }else{
                    Thread.sleep(1000);
                    maxWaiting -= 1000;
                }
            } while (maxWaiting >= 0);
            logger.error("Emulator hasn`t booted");
        } catch (Exception e) {
            logger.error("restart emulator failed", e);
        }
    }

}
