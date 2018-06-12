package com.qvc.selenium.steps;

import com.qvc.selenium.actions.Action;
import com.qvc.selenium.actions.ActionFactory;
import com.qvc.selenium.data.StoredValueDataManager;
import com.qvc.selenium.plugin.TestModule;
import com.qvc.selenium.plugin.TestState;
import com.qvc.selenium.reporting.ExecuteResult;
import com.qvc.selenium.reporting.HtmlTestReporter;
import org.apache.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;

public class IfContainsElementsTestStep extends TestStep {

    protected static StoredValueDataManager data = StoredValueDataManager
            .getInstance();

    private ArrayList<HashMap<String, String>> flow;
    private int startStep;
    private int endStep;
    private static final Logger logger = Logger.getLogger(IfContainsElementsTestStep.class.getName());

    public IfContainsElementsTestStep(RemoteWebDriver driver, int stepNumber,
                                      int endStep, ArrayList<HashMap<String, String>> flow, HtmlTestReporter reporter, Boolean topLevel, HashMap<String, Object> testData) throws InterruptedException {

        super(driver, 0, flow.get(0), reporter, topLevel, testData);
        setCurrentPageObjectFromFile();
        logger.debug("IF Contains Elements Step Constructor");
        setFlow(flow);
        setStartStep(stepNumber);
        setEndStep(endStep);
    }

    public ArrayList<HashMap<String, String>> getFlow() {
        return flow;
    }

    public void setFlow(ArrayList<HashMap<String, String>> flow) {
        this.flow = flow;
    }

    @Override
    public void run() throws Exception {
        logger.debug("Looking for Elements");
        Action thisAction = ActionFactory.getAction(actionType);
        thisAction.setComment(comment);
        thisAction.setDriver(getDriver());
        thisAction.setObjectName(object);
        thisAction.setPage(currentPageObject);
        thisAction.setActionType(actionType);
        thisAction.setStepData(testStepData);
        currentPageObject.setTestStepData(testStepData);
        thisAction.setLogSearchControlError(false);
        
        String testStepInputs = "";
		if (testStepInputData != null && !testStepInputData.isEmpty())
			testStepInputs = testStepInputData.toString();
		//Add log for if condition
		String locator = currentPageObject.getCustomLocator(object).toString();
		LOGGER.info((comment == null ? "" : comment + "\n ") +
                "Running action " + actionType +
                (object == null ? "" : " on " + object + " " + locator) +
                (testStepInputs.isEmpty() ? "" : " with data " + testStepInputs));
        try {
        	ExecuteResult result = thisAction.runAction();
			// Add report for if condition, Simon 5/9/2016
			passed = result.isResult();
			result.setStatus("Passed");//for condition check, always set to passed - not impact the test case execution status 
			
			reporter.appendResult(getDriver(), getStepNum() + 1,
					result, comment, page, object
							+ " <span style=\"color:green;\">"
							+ getLastLocator(thisAction) + "</span>",
					testStepInputs);
			
            if (passed) {
                // TODO: fail on class cast exception

                ArrayList<HashMap<String, String>> BlockBody = new ArrayList<HashMap<String, String>>();
                // When we have a loop we want to run the body of that loop as a sub
                // test until
                // The loop step condition is resolved or the Max runs value is met.
                // TODO: Validate there is a valid max runs value.

                BlockBody.addAll(flow.subList(1, flow.size() - 1));

                logger.debug("Block is " + flow.size() + " Block body has size of "
                        + BlockBody.size());

                this.setState(TestState.CONDITION);

                logger.debug("Running block count : ");

                TestModule loopTC = new TestModule();
                loopTC.setDriver(getDriver());
                loopTC.setTestFlow(BlockBody);
                loopTC.setReporter(reporter);
                loopTC.setTestPackage((String) data.getStoredData("testPackage"));
                loopTC.setEnv((String) data.getStoredData("env"));
                loopTC.setUi((String) data.getStoredData("ui"));
                loopTC.setBrowserType((String) data.getStoredData("client"));
                loopTC.setTestData(testStepData);
                loopTC.runTestFlow();
            }
        }catch (NoSuchElementException e){
            logger.debug("if contains locator not found");
        }
        this.setState(TestState.COMPLETE);
        this.passed = true;
    }

    public int getStartStep() {
        return startStep;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    @Override
    public int getEndStep() {
        return endStep;
    }

    @Override
    public void setEndStep(int endStep) {
        this.endStep = endStep;
    }

}
