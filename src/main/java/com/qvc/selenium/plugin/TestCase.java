package com.qvc.selenium.plugin;

import com.qvc.selenium.data.PageObjectManager;
import com.qvc.selenium.data.TestDataManager;
import com.qvc.selenium.data.TestManager;
import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.reporting.HtmlTestReporter;
import com.qvc.selenium.reporting.ReportNGTestLayout;
import com.qvc.selenium.reporting.SummaryReporter;
import io.appium.java_client.AppiumDriver;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestCase extends TestModule implements org.testng.ITest {

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());
    private SummaryReporter summary;
    private static int counter = 0;
    private static final int RESTART_EMULATOR_AFTER = 2;

    @Test(timeOut = 1800000)
    public void runTestCase() throws Exception {

        String retriesNumber = System.getProperty("retries");
        int maxRetries = 0;
        if (retriesNumber != null) {
            try{
                maxRetries = Integer.parseInt(retriesNumber);
            } catch (Exception e){
                logger.error("Incorrect value for \"Number of retries\": \"" + retriesNumber +
                        "\". Please specify integer value (>= 0).");
                e.printStackTrace();
                throw e;
            }

        }
        Exception err = null;

        try {
            testSetup();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        for (int i = 0; i < maxRetries + 1; i++) {
            err = null;
            if (i > 0) {
                logger.info("Retrying the test ... ");
                testSetup();
            }
            try {
                runTestFlow(true);
                super.teardown();
                break;
            }catch (InterruptedException e) {
                err = e;
                super.teardown();
                break;
            }catch (Exception e) {
                err = e;
                super.teardown();
            }
        }
        getDriver().quit();
        if (err != null) {
            err.printStackTrace();
            throw err;
        }

        if (!passed)
            throw new Exception(String.format("Test \"%s\" was failed.", getTestName()));
    }

    private void testSetup() throws Exception {
        TestManager.clearCache();
        TestDataManager.clearCache();
        PageObjectManager.clearCache();
        // indicate test name for current thread (for logging)
        Appender testNGAppender = Logger.getRootLogger().getAppender("TestNG");
        ReportNGTestLayout logLayout = (ReportNGTestLayout) testNGAppender.getLayout();
        logLayout.threadTestName.put(Long.toString(Thread.currentThread().getId()), getTestName());

        //get the data sheet and replace the productNum with actual value
        if (testData == null){
            logger.info("No test data for \""+getTestName()+"\"");
        } else {
            // Append data to singleton of current thread
            for (Map.Entry<String, Object> entry : testData.entrySet()) {
                data.setStoredData(entry.getKey().toLowerCase(), entry.getValue());
            }
            data.setStoredData("testCaseData", testData.clone());
            testData = null;
        }
        data.setStoredData("testPackage", this.getTestPackage());
        data.setStoredData("env", this.getEnv());
        data.setStoredData("ui", this.getUi());
        data.setStoredData("client", this.getBrowserType());
        data.setStoredData("deviceName", this.getDeviceName());
        data.setStoredData("app", this.getAppPath());

        //New a reporter
        setReporter(new HtmlTestReporter(this.getTestName(), getTestFlow().size(), env, ui, this.getBrowserType(), logLayout.executionDate, (logLayout.consoleLevel == Level.DEBUG_INT)));

        getReporter().appendHeader((HashMap<String, Object>) data.getStoredData("testCaseData"));

        HashMap<String, String> driverOptions = new HashMap<String, String>();
        driverOptions.put("app", this.getAppPath());
        driverOptions.put("client", this.getBrowserType());
        driverOptions.put("ui", this.getUi());
        driverOptions.put("env", this.getEnv());
        driverOptions.put("deviceName", this.getDeviceName());
		
//        if (counter == RESTART_EMULATOR_AFTER && this.getBrowserType().equalsIgnoreCase("android")){
//            QVCAndroidDriver.restartEmulator(this.getDeviceName());
//            counter = 0;
//        }else{
//            ++counter;
//        }

        setDriver(WebDriverManager.getSingletonManager().buildDriverInstance(getBrowserType(), getPlatform(), false, getTargetTestServer(), driverOptions));
    }

    private void restartApp() {
        if (getDriver() instanceof AppiumDriver)
            ((AppiumDriver) getDriver()).resetApp();
    }

    @Override
    public String getTestName() {
        return testName;
    }

    @BeforeSuite
    public void setupSummaryReport() {
        ReportNGTestLayout mobileLayout = (ReportNGTestLayout) Logger.getRootLogger().getAppender("TestNG").getLayout();
        String executionDate = mobileLayout.executionDate;
        summary = new SummaryReporter(executionDate, System.getProperty("executeSet"));
//        summary = new SummaryReporter(executionDate, suiteName);//because the executeSet, may continue multi suiteName(e.g. Checkout,ProductDetail, Navigation...), changed to use executeSet
    }

    @AfterSuite
    public void createSummaryReport() throws URISyntaxException, IOException {
        summary.createReport();
    }
}
