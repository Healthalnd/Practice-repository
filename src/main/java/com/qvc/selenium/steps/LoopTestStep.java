package com.qvc.selenium.steps;

import com.qvc.selenium.plugin.TestModule;
import com.qvc.selenium.actions.Action;
import com.qvc.selenium.actions.ActionFactory;
import com.qvc.selenium.plugin.TestState;
import com.qvc.selenium.reporting.ExecuteResult;
import com.qvc.selenium.reporting.HtmlTestReporter;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;

public class LoopTestStep extends TestStep {
	private ArrayList<HashMap<String, String>> flow;

	private static final Logger logger = Logger.getLogger(LoopTestStep.class.getName());

	public LoopTestStep(RemoteWebDriver driver, int stepNumber, int endStep,
			ArrayList<HashMap<String, String>> flow, HtmlTestReporter reporter,
			Boolean topLevel, HashMap<String, Object> testData)
			throws InterruptedException {
		super(driver, 0, flow.get(0), reporter, topLevel, testData);// add by
																	// Simon,
																	// 10/28/2015
		logger.debug("Loop Step Constructor");
		setTopLevel(topLevel);
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
		logger.debug("Running Loop of size " + flow.size());
		ArrayList<HashMap<String, String>> loopBody = new ArrayList<HashMap<String, String>>();
		// When we have a loop we want to run the body of that loop as a sub
		// test until
		// The loop step condition is resolved or the Max runs value is met.
		// TODO: Validate there is a valid max runs value.
		loopBody.addAll(flow.subList(1, flow.size() - 1));

		Action thisAction = ActionFactory.getAction(actionType);

		thisAction.setComment(comment);
		thisAction.setDriver(getDriver());
		thisAction.setObjectName(object);
		thisAction.setPage(currentPageObject);
		thisAction.setActionType(actionType);
		thisAction.setStepData(testStepData);
		// We are in a loop. Run the action. If we are still in a loop
		// after the action is performed we run the loop.

		this.setState(TestState.LOOP);

		int currentLoop = 1;
		String testStepInputs = "";
		if (testStepInputData != null && !testStepInputData.isEmpty())
			testStepInputs = testStepInputData.toString();
		//Add log for loop condition
		String locator = currentPageObject.getCustomLocator(object).toString();
		LOGGER.info((comment == null ? "" : comment + "\n ") +
                "Running action " + actionType +
                (object == null ? "" : " on " + object + " " + locator) +
                (testStepInputs.isEmpty() ? "" : " with data " + testStepInputs));
		do {

			setStepNum(this.getStartStep());
			try {

				ExecuteResult result = thisAction.runAction();
				// Add report for loop condition, Simon 5/9/2016
				passed = result.isResult();
				result.setStatus("Passed");//for condition check, always set to passed - not impact the test case execution status 
				
				reporter.appendResult(getDriver(), getStepNum() + 1,
						result, comment, page, object
								+ " <span style=\"color:green;\">"
								+ getLastLocator(thisAction) + "</span>",
						testStepInputs);
				
				if (result.isResult()) {
					this.setState(TestState.COMPLETE);
				} else {
					// Run the loop one time as long as we have not
					// reached our max loop
					if (Integer.valueOf(this.getOptions().get("maxLoop")) > currentLoop) {
						// Create a new Test case that is populated with the
						// loop;
						TestModule loopTC = new TestModule();
						loopTC.setDriver(getDriver());
						loopTC.setTestFlow(loopBody);
						loopTC.setReporter(getReporter());
						loopTC.setTestPackage((String) data
								.getStoredData("testPackage"));
						loopTC.setEnv((String) data.getStoredData("env"));
						loopTC.setUi((String) data.getStoredData("ui"));
						loopTC.setBrowserType((String) data
								.getStoredData("client"));
						loopTC.runTestFlow();
						// Jump the step count

					} else {
						// We are done here
						this.setState(TestState.COMPLETE);
					}

				}
				currentLoop++;
			} catch (Throwable e) {
				String msg = e.getMessage();
				ExecuteResult result = new ExecuteResult();
				result.setResult(false);
				result.setStatus("Failed");
				result.setStepDetail(thisAction.getActionType());
				reporter.appendExceptionInfo(getDriver(), getStepNum() + 1,
						result, comment, page, object, testStepInputs, msg,
						getLastLocator(thisAction));
				throw e;
			}
		} while (this.getState() != TestState.COMPLETE);

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
