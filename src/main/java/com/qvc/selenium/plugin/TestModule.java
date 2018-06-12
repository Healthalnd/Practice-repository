package com.qvc.selenium.plugin;

import com.qvc.selenium.data.StoredValueDataManager;
import com.qvc.selenium.reporting.HtmlTestReporter;
import com.qvc.selenium.reporting.TestNGAppender;
import com.qvc.selenium.steps.TestStep;
import com.qvc.selenium.steps.TestStepFactory;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.HashMap;

public class TestModule {

	private static final Logger logger = Logger.getLogger(TestModule.class.getName());
	protected int state;
	protected String testName;
	protected RemoteWebDriver driver;
	protected ArrayList<HashMap<String, String>> testFlow;
	protected String platform;
	protected String targetTestServer;
	protected String browserType;
	protected HashMap<String, Object> storedTestData;
    protected HashMap<String, Object> testData;
	protected static StoredValueDataManager data = StoredValueDataManager
			.getInstance();

	protected String env;
    protected String appPath;
    protected String ui;
    protected String testPackage;
    protected HtmlTestReporter reporter;
    protected String suiteName;
    protected String deviceName;

    public boolean passed = false;

    public String getTestPackage() {
        return testPackage;
    }

    public void setTestPackage(String testPackage) {
        this.testPackage = testPackage;
    }

    public void runTestFlow() throws Exception {
        runTestFlow(false);
    }

    public void runTestFlow(Boolean topLevel) throws Exception {

        passed = true;
        for (int i = 0; i < testFlow.size(); i++) {
            if(testFlow.get(i).size()==0)
                continue;

            // Stop execution if the job was stopped
            TestNGAppender testNGAppender = (TestNGAppender) Logger.getRootLogger().getAppender("TestNG");
            if (testNGAppender == null)
                throw new InterruptedException("Execution was stopped.");

            logger.debug("Start Running Steps");
            TestStep curStep = null;
            logger.debug(getTestPackage() + " - " + getUi() + " - ");
            curStep = TestStepFactory.getStep(getDriver(), i, getTestFlow(), getTestPackage(), getEnv(), getUi(), getBrowserType(), getReporter(), topLevel, testData);

            if (curStep == null) {
                if (topLevel) {
                    reporter.countEmptyModule();
                }
                continue;
            }

            logger.debug("Test Step: " + curStep.getAction());

            // So some steps might be to run other tests.
            curStep.run();

            if (!curStep.passed)
                passed = false;

            if (curStep.getAction().equalsIgnoreCase("LOOP")) {
                // We just ran a loop and need to jump to the end of it.
                i = curStep.getEndStep();
            } else if (curStep.getAction().equalsIgnoreCase("IFCONTAINS")) {
                i = curStep.getEndStep();
            }
        }


    }

    public void teardown() {
        logger.debug("After Test");
        System.setProperty("DT_AGENTACTIVE", "false");
        data.clearCache(); // clear current test case's data
        getDriver().quit();
        
        // Initiate garbage collection
        System.gc();
        logger.debug("Initiate garbage collection AfterTest");
        
        //create the report
        getReporter().createReport();
       
        
    }

	public RemoteWebDriver getDriver() {
		return driver;
	}

	public void setDriver(RemoteWebDriver driver) {
		this.driver = driver;
	}

	public ArrayList<HashMap<String, String>> getTestFlow() {
		return testFlow;
	}

	public void setTestFlow(ArrayList<HashMap<String, String>> testFlow) {
		this.testFlow = testFlow;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getTargetTestServer() {
		return targetTestServer;
	}

	public void setTargetTestServer(String targetTestServer) {
		this.targetTestServer = targetTestServer;
	}

	public String getBrowserType() {
		return browserType;
	}

	public void setBrowserType(String browserType) {
		this.browserType = browserType;
	}

	public HashMap<String, Object> getStoredTestData() {
		return storedTestData;
	}

	public void setStoredTestData(HashMap<String, Object> storedTestData) {
		this.storedTestData = storedTestData;
	}

	public void setTestCaseName(String name) {
		this.testName = name;
	}

    public String getUi() {
        return ui;
    }

    public void setUi(String ui) {
        this.ui = ui;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public HtmlTestReporter getReporter() {
        return reporter;
    }

    public void setReporter(HtmlTestReporter reporter) {
        this.reporter = reporter;
    }

    public HashMap<String, Object> getTestData() {
        return testData;
    }

    public void setTestData(HashMap<String, Object> testData) {
        this.testData = testData;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
