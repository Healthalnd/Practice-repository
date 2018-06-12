package com.qvc.selenium.steps;

import com.qvc.selenium.plugin.TestModule;
import com.qvc.selenium.data.StoredValueDataManager;
import com.qvc.selenium.plugin.TestState;
import com.qvc.selenium.reporting.HtmlTestReporter;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.*;

public class LoopElementsTestStep extends TestStep {
	protected static StoredValueDataManager data = StoredValueDataManager
			.getInstance();
	private ArrayList<HashMap<String, String>> flow;
	private int startStep;
	private int endStep;
	private static final Logger logger = Logger.getLogger(LoopTestStep.class.getName());

	public LoopElementsTestStep(RemoteWebDriver driver, int stepNumber,
		int endStep, ArrayList<HashMap<String, String>> flow, HtmlTestReporter reporter, Boolean topLevel, HashMap<String, Object> testData) throws InterruptedException {
		super(driver, 0, flow.get(0), reporter, topLevel, testData);
		logger.debug("Loop Elements Step Constructor");
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

	@SuppressWarnings("unchecked")
	@Override
	public void run() throws Exception {
		List<WebElement> elements = null;

		elements = (List<WebElement>) testStepData.get((String) testStepData
				.get("loop"));

		// TODO: fail on class cast exception

		ArrayList<HashMap<String, String>> loopBody = new ArrayList<HashMap<String, String>>();
		// When we have a loop we want to run the body of that loop as a sub
		// test until
		// The loop step condition is resolved or the Max runs value is met.
		// TODO: Validate there is a valid max runs value.
		loopBody.addAll(flow.subList(1, flow.size() - 1));
		logger.debug("Loop is " + flow.size() + " Loop body has size of "
				+ loopBody.size());
		this.setState(TestState.LOOP);

		Iterator<WebElement> itr = elements.iterator();
		int x = 0;
		while (itr.hasNext()) {
			logger.debug("In Loop count : " + x);
			x++;

			data.setStoredData("element", itr.next());
            TestModule loopTC = new TestModule();
			loopTC.setDriver(getDriver());
			loopTC.setTestFlow(loopBody);
            loopTC.setReporter(getReporter());
			loopTC.setTestPackage((String) data.getStoredData("testPackage"));
			loopTC.setEnv((String) data.getStoredData("env"));
			loopTC.setUi((String) data.getStoredData("ui"));
			loopTC.setBrowserType((String) data.getStoredData("client"));
            loopTC.runTestFlow();
		}
		this.setState(TestState.COMPLETE);

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
