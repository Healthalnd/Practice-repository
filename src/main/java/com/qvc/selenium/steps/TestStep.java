package com.qvc.selenium.steps;

import com.qvc.selenium.actions.Action;
import com.qvc.selenium.actions.ActionFactory;
import com.qvc.selenium.data.PageObject;
import com.qvc.selenium.data.PageObjectManager;
import com.qvc.selenium.data.StoredValueDataManager;
import com.qvc.selenium.data.TestDataManager;
import com.qvc.selenium.plugin.TestState;
import com.qvc.selenium.reporting.ExecuteResult;
import com.qvc.selenium.reporting.HtmlTestReporter;
import com.qvc.selenium.util.CreateNewAccount;
import com.qvc.selenium.util.GenerateCreditCard;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestStep {

    protected static final Logger LOGGER = Logger.getLogger(TestStep.class
            .getName());
    protected int state;
    protected String action;
    protected String page;
    protected String object;
    protected String actionType;
    protected Map<String, String> options;
    protected int stepNum;
    protected int startStep;
    protected int endStep;
    protected String comment;
    protected RemoteWebDriver driver;
    protected PageObject currentPageObject;
    protected String dataSource;
    protected static StoredValueDataManager data = StoredValueDataManager
            .getInstance();
    protected HashMap<String, Object> testStepData;
    protected HashMap<String, Object> testStepInputData;
    protected HtmlTestReporter reporter;
    protected boolean topLevel;

    public boolean passed = false;


    public PageObject getCurrentPageObject() {
        return currentPageObject;
    }

    public void setCurrentPageObject(PageObject currentPageObject) {
        this.currentPageObject = currentPageObject;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public TestStep() {

    }

    public TestStep(RemoteWebDriver driver, int stepNumber,
                    Map<String, String> step, HtmlTestReporter reporter, Boolean topLevel, HashMap<String, Object> testData) throws InterruptedException {

        setStepNum(stepNumber);

        this.reporter = reporter;
        this.topLevel = topLevel;
        this.passed = false;

        HashMap<String, Object> curTestStepData = new HashMap<String, Object>();

        HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
        if (curTestCaseData != null) {
            curTestStepData.putAll(curTestCaseData);
        } else {
            LOGGER.debug("No test data for this test.");
        }

        if (testData != null)
            curTestStepData.putAll(testData);

        setStepData(curTestStepData);

        setDataSource(Paths.get((String) data.getStoredData("testPackage"), "data", (String) data.getStoredData("ui"), (String) data.getStoredData("env")).toString());

        setAction(step.get("Action"));

        setActionType(step.get("ActionType"));

        setPage(step.get("Page"));
        
        setCurrentPageObjectFromFile();

        setObject(step.get("Object"));

        setInputs(step.get("Inputs"));

        setComment(step.get("Comment"));

        setOptions(step.get("Options"));

        setDriver(driver);

        //for report
        setReporter(reporter);

        if (topLevel)
            reporter.appendModuleActionInfo(step.get("ActionType"), step.get("Object"));

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action.toUpperCase();
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCurrentPageObjectFromFile() {
        try {

            if (page != null) {
                this.page = page.trim();
                // Are we on the page for this test?
                LOGGER.debug("Page is " + page);
                if (!this.page.isEmpty()) {
                    PageObject currentPageObject;
                    String testPackage = (String) data.getStoredData("testPackage");
                    String currentPageFile = Paths.get(testPackage, "pageObjects", this.page + ".xlsx").toString();
                    if (!Paths.get(currentPageFile).toFile().exists())
                        currentPageFile = Paths.get(testPackage, "pageObjects", (String)data.getStoredData("ui"), this.page + ".xml").toString(); //Add country to the path, update by Simon
                    currentPageObject = PageObjectManager.getPage(currentPageFile, (String) data.getStoredData("ui"), (String) data.getStoredData("client"));
                    setCurrentPageObject(currentPageObject);
                }
            }
        } catch (InterruptedException i) {
            LOGGER.error("Error opening page object xml", i);
        }
    }

    public void run() throws Exception {

        setCurrentPageObjectFromFile();

        String locator = "";
        String testStepInputs = "";
        
        LOGGER.debug("Starting " + actionType);
        Action thisAction = null;
   try {
        thisAction = ActionFactory.getAction(actionType);
        thisAction.setComment(comment);
        thisAction.setDriver(getDriver());
        thisAction.setObjectName(object);
        if (currentPageObject != null) {
            currentPageObject.setTestStepData(testStepData);
            List<HashMap<String, String>> customLocator = currentPageObject.getCustomLocator(object);
            if (customLocator == null)
                locator = "";
            else
                locator = customLocator.toString();
        }
        thisAction.setPage(currentPageObject);
        thisAction.setActionType(actionType);
        thisAction.setStepData(testStepData);

        
        if (testStepInputData != null && !testStepInputData.isEmpty())
            testStepInputs = testStepInputData.toString();

        LOGGER.info((comment == null ? "" : comment + "\n ") +
                "Running action " + actionType +
                (object == null ? "" : " on " + object + " " + locator) +
                (testStepInputs.isEmpty() ? "" : " with data " + testStepInputs));

        ExecuteResult result = thisAction.runAction();
        passed = result.isResult();
        if (passed)
            reporter.appendResult(getDriver(), getStepNum() + 1, result, comment, page, object + " <span style=\"color:green;\">" + getLastLocator(thisAction) + "</span>", testStepInputs);
        else
            reporter.appendExceptionInfo(getDriver(), getStepNum() + 1, result, comment, page, object, testStepInputs, "", getLastLocator(thisAction));
        //end module
        if (topLevel)
            reporter.endModuleActionInfo();
            
	   } catch (NullPointerException eNull){//catch NullPointerException caused by actionType(Click,SENDKEYS, CONTAINSTEXT...) not recognized - Simon 12/15/2016
		   
		   ExecuteResult result = new ExecuteResult();
	       result.setResult(false);
	       result.setStatus("Failed");
	       result.setStepDetail("Action type \""+actionType+"\" was not recognized.");
	       String emsg = "NullPointerException was occured by Action Type not recognized, please correct your spell !";
		   reporter.appendExceptionInfo(getDriver(), getStepNum() + 1, result, comment, page, object, testStepInputs, emsg, locator);
		   throw eNull;
		   
        } catch (Throwable e) {
            String msg = e.getMessage();
            String msgOrig = "";
            String origMsgHeader = "Original message: ";
            if (msg != null) {
                int endIndex = msg.indexOf("Capabilities");
                int startIndex = msg.indexOf(origMsgHeader) + origMsgHeader.length();
                if (endIndex >= 0 && endIndex - startIndex >= 0)
                    endIndex = endIndex - 1;
                else
                    endIndex = msg.length();

                if (msg.contains(origMsgHeader)) {
                    msgOrig = msg.substring(startIndex, endIndex);
                }
            }
            if (!msgOrig.isEmpty())
                LOGGER.error(msgOrig);
            else
                msgOrig = msg;

            LOGGER.error(this.getStepNum() + ": " + this.getAction() + "-"
                    + this.getActionType() + " : " + this.getComment());
            ExecuteResult result = new ExecuteResult();
            result.setResult(false);
            result.setStatus("Failed");
            result.setStepDetail(thisAction.getActionType());
            reporter.appendExceptionInfo(getDriver(), getStepNum() + 1, result, comment, page, object, testStepInputs, msgOrig, getLastLocator(thisAction));
            //end module
            if (topLevel)
                reporter.endModuleActionInfo();
            throw e;
        }

        this.setState(TestState.COMPLETE);
    }

    protected String getLastLocator(Action action) {
        PageObject pageObject = action.getPageObject();
        if (pageObject != null && pageObject.getLastLocator() != null) {
            return pageObject.getLastLocator().toString();
        } else
            return null;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Map<String, String> getOptions() {
        return this.options;
    }

    public void setOptions(String options) {
        LOGGER.debug("Setting Options");
        this.options = parseOptions(options);
        LOGGER.debug("Done Setting Options");
    }

    private Map<String, String> parseOptions(String options) {
        LOGGER.debug("parsing Options");
        Map<String, String> map = null;

        if ((options != null) && !options.equalsIgnoreCase("")) {
            map = new HashMap<String, String>();

            for (String opt : options.split(";")) {
                LOGGER.debug("parsing " + opt);
                String[] vars = opt.split("=");
                map.put(vars[0], vars[1]);
            }
        }
        return map;
    }

    protected void setInputs(String input) {
        if (input != null && !input.trim().isEmpty()) {
            String[] raw = input.split(";");
            LOGGER.debug("Step input count: " + raw.length);
            this.testStepInputData = new HashMap<String, Object>();
            for (int i = 0; i <= raw.length - 1; i++) {
                LOGGER.debug("Processing: " + raw[i]);
                if (!raw[i].trim().isEmpty()) {
                    this.testStepInputData.putAll(substitueVar(raw[i]));
                }
            }
            if (testStepData == null) {
                this.testStepData = new HashMap<String, Object>();
            }
            testStepData.putAll(testStepInputData);
        }
    }

    protected HashMap<String, String> substitueVar(String input) {
        HashMap<String, String> inputData = new HashMap<String, String>();

        String[] testVar = input.split("=", 2);
        if (testVar.length == 1)
            testVar = new String[]{"label", testVar[0]};
        // if the value is a variable look it up
        String inputValue = testVar[1];
        LOGGER.debug("Check if we have a <> in :" + inputValue);
        for (int i = 0; i < 10; i++) {
            if (inputValue.contains("<") && inputValue.substring(inputValue.indexOf("<")).contains(">")) {
                LOGGER.debug("Var confirmed substring: " + 1 + " - "
                        + Integer.toString(inputValue.length() - 1));
                String varName = inputValue.substring(inputValue.indexOf("<") + 1, inputValue.indexOf(">", inputValue.indexOf("<")));
                // If there is an "_" it's located in a special hash
                LOGGER.debug("Parsing Var :" + varName);
                if (varName.contains("_") && TestDataManager.getProperty(varName.toLowerCase(), getDataSource()) != null) {
                    String[] varparts = varName.split("_");
                    LOGGER.debug("Getting from test data " + varName.toLowerCase());
                    inputValue = TestDataManager.getProperty(varName.toLowerCase(), getDataSource());
                    LOGGER.debug("Storing Var :" + varparts[0].toLowerCase().trim()
                            + " - "
                            + inputValue);
                    inputData.put(varparts[0].toLowerCase().trim(),
                            inputValue);

                } else if (varName.startsWith("NewEmail")) {
                    String newEmail = CreateNewAccount.generateEmailAddress();
                    data.setStoredData("email", newEmail);

                    // Update test case data
                    HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
                    curTestCaseData.put("email", newEmail);
                    data.setStoredData("testCaseData", curTestCaseData);
                    inputValue = newEmail;
                } else if (varName.startsWith("NewPassword")) {
                    String newPassword = CreateNewAccount.generatePassword();
                    data.setStoredData("password", newPassword);
                    inputValue = newPassword;
                } else if (varName.startsWith("NewAnswer")) {
                    String newAnswer = CreateNewAccount.generateSecurityAnswer();
                    data.setStoredData("answer", newAnswer);
                    inputValue = newAnswer;
                } else if (varName.startsWith("NewHouseNumber")) {
                    String newHouseNum = CreateNewAccount.generateHouseNumber();
                    data.setStoredData("housenumber", newHouseNum);
                    inputValue = newHouseNum; 
                } else if (varName.startsWith("NewCard")) {
//                    String newCardNumber = GenerateCreditCard
//                            .generateCC(TestDataManager.getProperty("cardtype", getDataSource()));
                	String newCardNumber = GenerateCreditCard
                            .generateCC(testStepData.get("cardtype").toString());//update by Simon, 11/2/2015
                	String cclastfournumber = newCardNumber.substring(newCardNumber.length()-4, newCardNumber.length());
                	data.setStoredData("creditcard", newCardNumber);
    				data.setStoredData("cclastfournumber", cclastfournumber);
                    inputData.put("creditcard", newCardNumber);
                    inputData.put("cclastfournumber", cclastfournumber);
                    inputValue = newCardNumber;
                } else if (testStepData != null && testStepData.containsKey(varName.toLowerCase())) {
                    LOGGER.debug("Getting from step data " + varName);
                    inputValue = inputValue.replace("<" + varName + ">",
                            (CharSequence) testStepData.get(varName.toLowerCase()));
                } else if (data.getStoredData(varName.toLowerCase()) != null) {
                    inputValue = inputValue.replace("<" + varName + ">", (String) data.getStoredData(varName.toLowerCase()));
                    LOGGER.debug("Adding to test run data from test run data{"
                            + varName.toLowerCase() + "} "
                            + testVar[0].toLowerCase() + " - "
                            + inputValue);
                } else {
                    LOGGER.debug("Adding to test run data  " + testVar[0].toLowerCase()
                            + " - " + inputValue);
                }
            } else
                break;
        }
        LOGGER.debug("Adding to test run data  " + testVar[0].toLowerCase()
                + " - " + inputValue);
        inputData.put(testVar[0].toLowerCase().trim(), inputValue);

        return inputData;
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public int getEndStep() {
        return endStep;
    }

    public void setEndStep(int endStep) {
        this.endStep = endStep;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setStepData(HashMap<String, Object> testStepData) {
        this.testStepData = testStepData;
    }

    public HashMap<String, Object> getStepData() {
        return this.testStepData;
    }

    public HashMap<String, Object> getStepInputData() {
        return this.testStepInputData;
    }

    public HtmlTestReporter getReporter() {
        return reporter;
    }

    public void setReporter(HtmlTestReporter reporter) {
        this.reporter = reporter;
    }

    public void setTopLevel(Boolean topLevel) {
        this.topLevel = topLevel;
    }

}
