package com.qvc.selenium;

import com.qvc.selenium.plugin.TestSuiteGenerator;
import com.qvc.selenium.reporting.ReportNGTestLayout;
import com.qvc.selenium.reporting.TestNGAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class LocalTestSuiteRunner {

    private static final Logger logger = Logger.getLogger(LocalTestSuiteRunner.class.getName());
    private static Hashtable<String, Object> runConfig;
    private static final String JENKINS_BUILD_Number = System.getProperty("jenkins.buildNumber");
	
    public static void main(String[] args) throws Exception {
    	System.setProperty("reportRootPath", "testOutput");
//        System.setProperty("MobBuildsDir", "/Users/00917482/Documents/Builds");//not use it right now
    	
//    	get the config from args "arg1[grid] arg2[suite] arg3[testPackage] arg4[env] arg5[threadCount] arg6[rerun] arg7[logLevel]"
    	String grid = args[0];
    	String suite = args[1];
    	String testPackage = args[2];
    	String env = args[3];
    	String threadCount = args[4];
    	String rerun = args[5];
    	String loglevel = args[6];
    	
        runConfig = new Hashtable<>();
        runConfig.put("grid", grid);//stormking.qvcdev.qvc.net
        runConfig.put("suite", suite);
        runConfig.put("testPackage", testPackage);
        runConfig.put("env", env);
        runConfig.put("threadCount", threadCount);
        runConfig.put("rerun", rerun);
        setUpLogger(loglevel);
    	
    	/*runConfig = new Hashtable<>();
        runConfig.put("suite", "Smoke");
        runConfig.put("grid", "localhost");//stormking.qvcdev.qvc.net
        runConfig.put("testPackage", "Q:/EnableIT/SeleniumTesterPlugin/QVCWebsite_China");
        runConfig.put("rerun", "yes");
        runConfig.put("env", "QA");
        runConfig.put("threadCount", "5");
        setUpLogger("INFO");*/
        
        // set filters - empty mean not set filter
        runConfig.put("country", "");
        runConfig.put("os", "");
        runConfig.put("client", "");
        runConfig.put("deviceName", "");
        
        
        if (runConfig.get("rerun") != null && runConfig.get("rerun").toString().equalsIgnoreCase("auto")) {
			// 1. running all test cases
			runTest();
			// 2. re-run failed and not completed test cases once
			if (System.getProperty("RunningStatus").equalsIgnoreCase(
					"NotCompleted")) {
				runConfig.put("rerun", "yes");
				System.setProperty("rerun", "yes");
				runTest();
			}

		} else if(runConfig.get("rerun") != null && runConfig.get("rerun").toString().equalsIgnoreCase("yes")) {
			System.setProperty("rerun", "yes");
			runTest();
			
		}else{
			runTest();
		}
        
       

    }


    private static void runTest() throws Exception {
    	long startTime ;
    	long endTime;
		// TODO Auto-generated method stub
    	 logger.debug("Starting Test Suite run");
         startTime = System.currentTimeMillis();
         XmlSuite suite = TestSuiteGenerator.buildSuite(runConfig);
         endTime = System.currentTimeMillis();

         logger.info(String.format("Build Suite Execution Time %d min, %d sec",
                         TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) / 60,
                         TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) % 60)
         );

         TestListenerAdapter tla = new TestListenerAdapter();
         TestNG testng = new TestNG();
         testng.setOutputDirectory("testOutput");
         testng.addListener(tla);
         testng.setCommandLineSuite(suite);

         logger.debug("Starting Test Suite run");
         startTime = System.currentTimeMillis();
         testng.run();
         endTime = System.currentTimeMillis();

         logger.info(String.format(" Execution Time %d min, %d sec",
                         TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) / 60,
                         TimeUnit.MILLISECONDS.toSeconds(endTime - startTime) % 60)
         );
	}


	private static void setUpLogger(String logLevel) {
        Logger rootLogger = Logger.getRootLogger();
		if (logLevel.equalsIgnoreCase("INFO")) {
			//temporally set it to ERROR - Simon 3/7/2018
			rootLogger.setLevel(Level.ERROR);
		}
		else if (logLevel.equalsIgnoreCase("WARN")) {
			rootLogger.setLevel(Level.WARN);
		} else if (logLevel.equalsIgnoreCase("ERROR")) {
			rootLogger.setLevel(Level.ERROR);
		} else
			rootLogger.setLevel(Level.DEBUG);
        TestNGAppender testNGAppender = new TestNGAppender("TestNG");
        ReportNGTestLayout logLayout = new ReportNGTestLayout();
		if(JENKINS_BUILD_Number == null)//if we not have build number, use time stamp
        	logLayout.executionDate = new SimpleDateFormat("MM-dd-yyyy-HH-mm").format(new Date());
        else
        	logLayout.executionDate = JENKINS_BUILD_Number;
       
        logLayout.reportParentFolder = Paths.get(System.getProperty("reportRootPath"), logLayout.executionDate).toString();
        logLayout.consoleLevel = Level.toLevel(logLevel.toUpperCase(), Level.DEBUG).toInt();
        testNGAppender.setLayout(logLayout);
        rootLogger.addAppender(testNGAppender);
        // Log in console in and log file
        logger.debug("Log4j appender configuration is successful !!");
    }

}
