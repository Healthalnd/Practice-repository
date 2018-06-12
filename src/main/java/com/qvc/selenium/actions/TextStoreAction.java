package com.qvc.selenium.actions;

import com.qvc.selenium.reporting.ExecuteResult;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Save text from current element to testData map using name from "storeas" value.
Inputs:
	storeas - (required) variable name for extracted text
    regex - (optional) string with regular expression for extracting value from control text (optionally).
        Values should be like "(\d+)".
 */
public class TextStoreAction extends BaseAction {
    @Override
    public ExecuteResult runAction() throws Exception {

        super.logAction();
        ExecuteResult stepResult = new ExecuteResult();
        //use to store the text info, such as, orderNum, itemPrice....
        String name = (String) testData.get("storeas");
        String regex = (String) testData.get("regex");
        name = name.toLowerCase();
        WebElement we=getWebElement();
        String value = we.getText();

        if ((value == null) || value.isEmpty()) {
            value = we.getAttribute("value");
        }

        if ((value == null) || value.isEmpty()) {
            value = we.getAttribute("text");
        }

        String regexMSG = "";
        if (regex != null) {
            regexMSG = " (using regex:'" + regex + "' for value:'" + value + "')";
            value = findByRegEx(regex, value);
        }
        data.setStoredData(name, value);

        // Update test case data
        HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
        curTestCaseData.put(name, value);
        data.setStoredData("testCaseData", curTestCaseData);

        logger.debug("text store" + regexMSG + ": " + name + " - " + value);

        stepResult.setResult(true);
        stepResult.setStatus("Passed");
        stepResult.setStepDetail("text store" + regexMSG + ": " + name + " - " + value);

        return stepResult;
    }

    private String findByRegEx(String regex, String value) {
        String res = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            res = matcher.group(0);
        }
        return res;
    }
}
