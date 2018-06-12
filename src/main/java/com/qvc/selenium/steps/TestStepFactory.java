package com.qvc.selenium.steps;

import com.qvc.selenium.data.TestManager;
import com.qvc.selenium.reporting.HtmlTestReporter;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class TestStepFactory {
    private static final Logger logger = Logger.getLogger(TestStepFactory.class
            .getName());

    public static TestStep getStep(RemoteWebDriver driver, int step,
                                   ArrayList<HashMap<String, String>> flow, String testPackage, String env, String ui, String client, HtmlTestReporter reporter, Boolean topLevel, HashMap<String, Object> testData) throws Exception {

        String requestedAction = flow.get(step).get("Action");
        String requestedActionType = flow.get(step).get("ActionType");
        if (requestedAction == null || requestedAction.isEmpty())
            return null;

        logger.debug("requested : " + requestedActionType);

        if (requestedActionType.equalsIgnoreCase("VerifyEmail&ShipToListExist")) {
            logger.debug("use for debug code - requested : " + requestedActionType);
        }

        TestStep ts = null;
        ArrayList<HashMap<String, String>> function = null;
        // So some steps might be to run other tests.
        if (requestedAction.equalsIgnoreCase("FUNCTION")) {
            logger.debug("Creating Test Step " + requestedActionType);
            String modulePathWithName = Paths.get(testPackage, "module", flow.get(step).get("ActionType")).toString();
          /* compatible with Web Test Cases that created before, Module: one excel with multi sheets as Action Rows
           * (e.g. ProductDetail module with sheets: AddToCart, Speedbuy, AddToWishList...)
           * So here add a if statement 
           * Simon Zhang - 3/3/2016
           */
            if (!Paths.get(modulePathWithName + ".xlsx").toFile().exists()){
            	//if the file not exist, add country to the page, then load the file - Simon Zhang, 10/28/2015
            	modulePathWithName = Paths.get(testPackage, "module", ui, flow.get(step).get("Object")).toString();
            	function = TestManager.getTestXSLFile(modulePathWithName).get(requestedActionType.toUpperCase());
            }else{
            	//Load the excel format created by EPAM
            	function = getSheetByUiAndClient(TestManager.getTestXSLFile(modulePathWithName)
                    , ui, client);
            }

            if (function == null)
            {
                logger.warn("No sheet for Country=\"" + ui + "\" and Client=\"" + client + "\" in " + modulePathWithName);
                function = new ArrayList<HashMap<String, String>>();
            }

            if (function.size()>0 || topLevel) {
                logger.debug("Creating Function of size : " + function.size());
                ts = new FunctionTestStep(driver, step, flow.get(step), function, reporter, topLevel, testData);
                logger.debug("Test Step Created");
            }

        } else if (requestedAction.equalsIgnoreCase("LOOP")) {
            logger.debug("Building loop");
            // Find the end of the current loop

            int endStepNum = step;
            int loopCount = 1;
            logger.debug("Loop Step num is - " + endStepNum);
            do {
                endStepNum++;

                if (flow.get(endStepNum).get("Action").equalsIgnoreCase("LOOP")) {
                    loopCount++;
                } else if (flow.get(endStepNum).get("Action")
                        .equalsIgnoreCase("ENDLOOP")) {
                    loopCount--;
                }

                logger.debug("Loop count -> " + loopCount + " end step is "
                        + endStepNum);
            } while (loopCount != 0);

            ArrayList<HashMap<String, String>> loop = new ArrayList<HashMap<String, String>>();
            loop.addAll(flow.subList(step, endStepNum + 1));
            logger.debug("Loop built for steps " + step + " to " + endStepNum);

            if (requestedActionType.equalsIgnoreCase("ELEMENTS")) {
                ts = new LoopElementsTestStep(driver, step, endStepNum, loop, reporter, topLevel, testData);
            } else {
                ts = new LoopTestStep(driver, step, endStepNum, loop, reporter, topLevel, testData);
            }

        } else if (requestedAction.equalsIgnoreCase("IFCONTAINS")) {
            logger.debug("Building If Block");
            // Find the end of the current if statement

            int endStepNum = step;
            int blockCount = 1;
            logger.debug("Block Step num is - " + endStepNum);
            do {
                endStepNum++;

                if (flow.get(endStepNum).get("Action").equalsIgnoreCase("IFCONTAINS")) {
                    blockCount++;
                } else if (flow.get(endStepNum).get("Action")
                        .equalsIgnoreCase("ENDIFCONTAINS")) {
                    blockCount--;
                }

                logger.debug("Block count -> " + blockCount + " end step is "
                        + endStepNum);
            } while (blockCount != 0);

            ArrayList<HashMap<String, String>> loop = new ArrayList<HashMap<String, String>>();
            loop.addAll(flow.subList(step, endStepNum + 1));
            logger.debug("Block built for steps " + step + " to " + endStepNum);

            if (requestedActionType.equalsIgnoreCase("CheckForElements")) {
                ts = new IfContainsElementsTestStep(driver, step, endStepNum, loop, reporter, topLevel, testData);
            } else {
                //ts = new IfContainsTestStep(driver, step, endStepNum, loop);
            }
        } else {
            // default is perform or wait
            logger.debug("Creating Default Step ");
            ts = new TestStep(driver, step, flow.get(step), reporter, topLevel, testData);
            logger.debug("Step created : " + ts.getActionType());
        }


        return ts;

    }

    private static ArrayList<HashMap<String, String>> getSheetByUiAndClient(HashMap<String, ArrayList<HashMap<String, String>>> allSheetsMap, String ui, String client) throws Exception {
        String currentSheetName = getCurrentSheetName(allSheetsMap.keySet().toArray(), ui, client);
        if (currentSheetName == null)
            return null;
        return allSheetsMap.get(currentSheetName);
    }

    private static String getCurrentSheetName(Object[] sheetNames, String ui, String client) throws Exception {
        if (sheetNames.length == 1)
            return (String) sheetNames[0];

        ArrayList<String> sheetNamesUpperCase = new ArrayList<>();
        for (Object sheetName : sheetNames) {
            sheetNamesUpperCase.add(((String) sheetName).toUpperCase());
        }

        ui = ui.toUpperCase();
        client = client.toUpperCase();

        ArrayList<String> possibleModules = new ArrayList<String>();
        possibleModules.add("MODULE_" + ui + "_" + client);
        possibleModules.add("MODULE_" + client + "_" + ui);
        possibleModules.add("MODULE_" + ui);
        possibleModules.add("MODULE_" + client);
        possibleModules.add("MODULE");

        for (String possibleModule : possibleModules) {
            if (sheetNamesUpperCase.contains(possibleModule)) {
                return (String) sheetNames[sheetNamesUpperCase.indexOf(possibleModule)];
            }
        }

        String msg = "Error in MODULE getCurrentSheetName for ui=" + ui + " client=" + client;
        logger.warn(msg);
        return null;
    }
}
