package com.qvc.selenium.drivers;

import io.appium.java_client.MobileElement;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;

import java.io.IOException;

public class QVCCustomMobileElement extends MobileElement {
    static final Logger logger = Logger.getLogger(QVCCustomMobileElement.class.getName());
    private Point p;
    private String xpath;
    private String winName;
    private String text;

    public QVCCustomMobileElement(QVCAndroidDriver qvcAndroidDriver, String xpath) {
        this.xpath = xpath;
        try {
            this.winName = xpath.split("title,'")[1].split("'")[0];
        } catch (Exception e) {
            logger.error("getWinName form '" + xpath + "' parse error.");
        }

        try {
            this.text = xpath.split("text='")[1].split("'")[0];
        } catch (Exception e) {
            logger.error("getText form '" + xpath + "' parse error.");
        }
        setParent(qvcAndroidDriver);
    }

    public QVCCustomMobileElement find() {
        try {
            p = PopupWindowReader.getCenterOfTextElement(winName, text);
            if (p != null) {
                logger.debug("Found AndroidPopupElement(xpath='" + xpath + "')");
                return this;
            }
        } catch (Exception e) {
            p = null;
        }
        logger.warn("Not Found AndroidPopupElement(xpath='" + xpath + "').");
        return null;
    }

    @Override
    public void click() {
        try {
            p = PopupWindowReader.getCenterOfTextElement(winName, text);
            logger.debug("try click to " + p);
            PopupWindowReader.tap(p);
            logger.debug("clickAction for AndroidPopupElement(xpath='" + xpath + "')' success");
        } catch (IOException e) {
            logger.error("clickAction Error for PopupElement:'" + xpath + "'");
        }
    }

    @Override
    public String getText() {
        return text;
    }
}
