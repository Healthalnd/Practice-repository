package com.qvc.selenium.plugin;

import com.qvc.selenium.data.TestManager;
import com.qvc.selenium.reporting.ReportNGTestLayout;
import com.qvc.selenium.reporting.TestNGAppender;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import jenkins.security.MasterToSlaveCallable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;


public class TestSuiteRunner extends MasterToSlaveCallable<Boolean, RuntimeException> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TestSuiteRunner.class.getName());
    private final BuildListener listener;
    private Hashtable<String, Object> runConfig = new Hashtable<String, Object>();
    private String buildID;

    private final String frameworkJAR;

    public TestSuiteRunner(
            final AbstractBuild build,
            final BuildListener listener,
            final String frameworkJAR,
            final String grid,
            final String suite,
            final String testPackage,
            final String threadCount,
            final String rerun,
            final String logLevel,
            final String env,
            final String country,
            final String os,
            final String client,
            final String deviceName
            ) throws IOException, InterruptedException {

        this.listener = listener;

        this.frameworkJAR = frameworkJAR;
        //final FilePath workspaceDir = build.getWorkspace();


        EnvVars ev = build.getEnvironment(listener);

        String workspacePath = ev.get("workspace");
        buildID = ev.get("BUILD_NUMBER");

        runConfig.put("threadCount", threadCount);
        runConfig.put("rerun", rerun);
        runConfig.put("env", ev);
        runConfig.put("suite", suite);
        runConfig.put("grid", grid);
        if (Files.exists(Paths.get(testPackage)))
            runConfig.put("testPackage", testPackage);
        else
            runConfig.put("testPackage", Paths.get(workspacePath, testPackage).toString());
        runConfig.put("logLevel", logLevel);
        runConfig.put("workspace", workspacePath);

        // set filters
        runConfig.put("env", env);
        runConfig.put("country", country);
        runConfig.put("os", os);
        runConfig.put("client", client);
        runConfig.put("deviceName", deviceName);

        System.setProperty("reportRootPath", Paths.get(workspacePath, "testOutput").toString());

    }


    public Boolean call() {

        TestManager.clearCache();
       /* TestManager.clearCache();
        TestDataManager.clearCache();
        PageObjectManager.clearCache();*/

        setUpLogger((String) runConfig.get("logLevel"));

        logger.debug("Using Framework:" + frameworkJAR);
        logger.debug("Calling SeleniumTester with the following settings:");
        logger.debug(runConfig.toString());

        try {
            /*

			listener.getLogger().println("[Selenium Framework Tester] Trying to do Stuff!");
			
			Runtime re = Runtime.getRuntime();
			BufferedReader output;

			Process ps = re.exec("java -jar " + frameworkJAR);
			ps.waitFor();
			output = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			listener.getLogger().println(output.readLine());
			*/

            if (frameworkJAR.equalsIgnoreCase("default")) {
                logger.debug("Trying to do Stuff!");

                runSuite();
            } else {
                logger.debug("Nothing here");
            }

        } catch (Exception e) {
            logger.debug("Exception Caught: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        logger.debug("Did Some Stuff!");
//        cleanUpLogger();
        return true;

    }

    public void runSuite() throws Exception {
		/**
		 * 12/03/2014  Add by Wen Zhang(Simon)
		 * 8/19/2016	recover the re-run function  - Simon
		 *Add configuration to control the re-run function, use Smoke for example:
		 *values for "rerun"
		 *No � running Smoke test cases(will generate NotPassed file, if all passed, this file will not generate)
		 *Yes � running NotPassed(Failed and Not Completed) test cases for Smoke set
		 *Auto � running Smoke test cases, when have not passed test cases, auto re-run those test cases once
		 */
		if (runConfig.get("rerun") != null && runConfig.get("rerun").toString().equalsIgnoreCase("auto")) {
			// 1. running all test cases
			runTest();
			// 2. re-run failed and not completed test cases once
			if (System.getProperty("RunningStatus").equalsIgnoreCase(
					"NotCompleted")) {
				runConfig.put("rerun", "yes");
				System.setProperty("rerun", "yes");
				setUpLogger((String) runConfig.get("logLevel"));
				runTest();
			}

		} else {
			runTest();
		}


    }

    private void runTest() throws Exception {
        //Create a test suite xml object from input suite

        XmlSuite suite = TestSuiteGenerator.buildSuite(runConfig);

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setOutputDirectory(((String) runConfig.get("workspace")) + "/testOutput");
        testng.addListener(tla);
        testng.setCommandLineSuite(suite);

        logger.debug("Starting Test Suite run");

        long startTime = System.currentTimeMillis();
        testng.run();
        long endTime = System.currentTimeMillis();


        for (int i = 0; i< 10; i++)
        {
            if ((tla.getPassedTests().size() + tla.getFailedTests().size() + tla.getSkippedTests().size()) == suite.getTests().size())
                break;
            Thread.sleep(2000);
        }

        logger.info(String.format(" Execution Time %d min, %d sec",
                        TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) / 60,
                        TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) % 60)
        );
        
//        cleanUpLogger();
    }

    private void setUpLogger(String logLevel) {
        Logger rootLogger = Logger.getRootLogger();
        if(logLevel.equalsIgnoreCase("INFO"))
        	rootLogger.setLevel(Level.INFO);
        else
        	rootLogger.setLevel(Level.DEBUG);
        TestNGAppender testNGAppender = new TestNGAppender("TestNG");
        ReportNGTestLayout logLayout = new ReportNGTestLayout();
        logLayout.executionDate = buildID;
        logLayout.reportParentFolder = Paths.get(System.getProperty("reportRootPath"), logLayout.executionDate).toString();
        logLayout.consoleLevel = Level.toLevel(logLevel.toUpperCase(), Level.DEBUG).toInt();
        testNGAppender.setLayout(logLayout);
        rootLogger.addAppender(testNGAppender);
        // Log in console in and log file
        logger.debug("Log4j appender configuration is successful !!");
    }

    public void cleanUpLogger() {
        Logger.getRootLogger().removeAllAppenders();

    }

}
