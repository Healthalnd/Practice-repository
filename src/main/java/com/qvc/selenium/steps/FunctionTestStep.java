package com.qvc.selenium.steps;

import com.qvc.selenium.plugin.TestModule;
import com.qvc.selenium.plugin.TestCase;
import com.qvc.selenium.reporting.HtmlTestReporter;
import io.appium.java_client.AppiumDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FunctionTestStep extends TestStep {
	private static final Logger logger = Logger.getLogger(FunctionTestStep.class.getName());
	ArrayList<HashMap<String, String>> function;
	RemoteWebDriver driver;
	private long startTime;
	private long endTime;
	private String functionElapsedTime;

	public FunctionTestStep(RemoteWebDriver driver, int stepNumber,
			HashMap<String, String> step,
			ArrayList<HashMap<String, String>> function, HtmlTestReporter reporter, Boolean topLevel, HashMap<String, Object> testData) throws InterruptedException {

		if (testStepData == null) {
			this.testStepData = new HashMap<String, Object>();
		}
		if (testData != null)
			testStepData.putAll(testData);

		this.passed = false;

        setTopLevel(topLevel);

		setStepNum(stepNumber);

		setAction(step.get("Action"));

		setActionType(step.get("ActionType"));

		setObject(step.get("Object"));

		setInputs(step.get("Inputs"));

		setComment(step.get("Comment"));

		setOptions(step.get("Options"));

		setDriver(driver);

        setPage(step.get("Page"));

		logger.debug("Setting  Function");

		setFunction(function);

        //for report
        setReporter(reporter);

        if (topLevel)
            reporter.appendModuleActionInfo(step.get("ActionType"), step.get("Object"));
	}

	@Override
	public void run() throws Exception {
		if (function.size()>0) {
			// Create a new Test case that is populated with the loop;
			TestModule functionTC = new TestModule();
			functionTC.setDriver(getDriver());
			functionTC.setTestFlow(function);
			functionTC.setTestCaseName(getComment());
			functionTC.setTestPackage((String) data.getStoredData("testPackage"));
			functionTC.setEnv((String) data.getStoredData("env"));
			functionTC.setUi((String) data.getStoredData("ui"));
			functionTC.setBrowserType((String) data.getStoredData("client"));
			functionTC.setReporter(reporter);
			functionTC.setTestData(testStepData);

			LOGGER.info((comment == null ? "" : comment + "\n ") +
					"Test module \"" + actionType + "\"" +
					((testStepData == null || testStepData.isEmpty()) ? "" : "\" with data: \"" + testStepData + "\"")
					);
			startTime = System.currentTimeMillis();
			functionTC.runTestFlow();
	        endTime = System.currentTimeMillis();
	        functionElapsedTime = String.format("Function Elapsed Time %d min, %d sec",
                    TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) / 60,
                    TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) % 60);

	        LOGGER.info(functionElapsedTime);

			passed = functionTC.passed;
		} else {
			passed = true;
		}
		
		//end module
        if (topLevel){
        	reporter.endModuleActionInfo();
        	reporter.appendFunctionElapsedTime(functionElapsedTime);
        }
	}

	public ArrayList<HashMap<String, String>> getFunction() {
		return function;
	}

	public void setFunction(ArrayList<HashMap<String, String>> function) {
		this.function = function;
	}

	@Override
	public RemoteWebDriver getDriver() {
		return driver;
	}

	@Override
	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}

}
