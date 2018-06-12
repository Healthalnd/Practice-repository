package com.qvc.selenium.plugin;

import com.qvc.selenium.data.TestManager;
import com.qvc.selenium.reporting.QVCReporter;
import org.apache.log4j.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class TestSuiteGenerator {


    private static final Logger logger = Logger.getLogger(TestSuiteGenerator.class.getName());


    public static XmlSuite buildSuite(Hashtable config) throws Exception {

        XmlSuite suite = new XmlSuite();
        String executeSet = (String) config.get("suite");//this is the actual suite we execute at testSet.xls file, Simon 5/13/2016
        System.setProperty("executeSet", executeSet);
        System.setProperty("testPackage", (String) config.get("testPackage"));
        suite.setName(executeSet);

		/*	1. parallel=”methods”: TestNG will run all the test methods in parallel in different threads. The dependent methods will also run in different threads but would respect the order that was specified.
		2. parallel=”tests”: TestNG will run all the methods in the same test in the same thread but each test will be run in separate threads. This allows to group the thread-safe classes in the same test so that they will run in the same thread while taking advantage of TestNG’s capability to run tests in several threads.
		3. parallel=”classes”: TestNG will run all the test methods in the same class in the same thread and each class will be run in separate thread.
		4. parallel=”instances”: TestNG will run all the test methods in the same instance in the same thread and each instance will be run in separate threads. */

        suite.setParallel("tests");
        
        int threadCount = 1;//set default value
        //Pull from Config
        if(config.get("threadCount") != null){
        	threadCount = Integer.valueOf(config.get("threadCount").toString()).intValue();
        }

        suite.setThreadCount(threadCount);
        String testSetName = "TestCaseSet";
        if(System.getProperty("jenkins.buildNumber") == null){
        	testSetName = "TestCaseSetTest";
        }
        	
        HashMap<String, ArrayList<HashMap<String, String>>> testSetHash = null;
        try
        {
        	if(config.get("rerun") != null && config.get("rerun").toString().equalsIgnoreCase("yes")){
        		File reRunSet = new File(Paths.get(System.getProperty("reportRootPath"), "NotPassed", "NotPassed_" + executeSet + ".xlsx").toString());
        		//If rerun set file exist, then we can re-run those failed test cases, otherwise loading TestCaseSet.xls and running all test cases - add by Simon, 9/7/2016
        		if(reRunSet.exists())
        			testSetHash = TestManager.getTestXSLFile(Paths.get(System.getProperty("reportRootPath"), "NotPassed", "NotPassed_" + executeSet).toString());
        		else
        			testSetHash = TestManager.getTestXSLFile(Paths.get((String) config.get("testPackage"), "tests", testSetName).toString());	
        	}else{
        		//Get a list of the tests we are going to run and build a suite with the tests as parameters for it.
        		testSetHash = TestManager.getTestXSLFile(Paths.get((String) config.get("testPackage"), "tests", testSetName).toString());
        		
        	}
        }
        catch(Exception e)
        {
            logger.error("Failed to open and parse suite file " + suite.getName(), e);
            throw e;
        }

        //Set HTML Reporter

        suite.addListener(QVCReporter.class.getName());


        //Get a list of files that contain one to many tests.
        ArrayList<HashMap<String, String>> testSet = testSetHash.get(suite.getName().toUpperCase());

        if (testSet == null)
            throw new Exception("Suite \""+suite.getName()+"\" was not found in "+ Paths.get((String) config.get("testPackage"), "tests", testSetName + ".xlsx").toString());

        int caseCount = testSet.size();


        logger.debug("Building Test Case Array - length: " + caseCount);

        for(int i = 0; i < caseCount; i++){
            HashMap<String, String> test = new HashMap<>();
            for (String testKey: testSet.get(i).keySet()) {
                test.put(testKey.toLowerCase(), testSet.get(i).get(testKey));
            }

            // skip disabled
            if (test.containsKey("disable") && !test.get("disable").trim().isEmpty())
                continue;

            HashMap<String, String> configFilters = new HashMap<String, String>();
            configFilters.put("country", "country");
            configFilters.put("os", "os");
            configFilters.put("client", "client");
            configFilters.put("deviceName", "device name");

            // skip filtered
            boolean skipFiltered = false;
            for ( String confFilterKey: configFilters.keySet()) {
                String testSetColumn = configFilters.get(confFilterKey);
                String confFilter = (String) config.get(confFilterKey);
                if (confFilter != null && !confFilter.isEmpty() && test.containsKey(testSetColumn) && !test.get(testSetColumn).equalsIgnoreCase(confFilter)){
                    skipFiltered = true;
                    break;
                }
            }
            if (skipFiltered)
                continue;

            String testName = test.get("testname")+"_"+test.get("country")+"_"+test.get("client");

            XmlTest xmlTest = new XmlTest(suite);
            xmlTest.setName(testName);
            logger.debug("adding to suite "+ testName);

            ArrayList <XmlClass> classes = new ArrayList <XmlClass>();

            XmlClass testClass = new XmlClass();

            testClass.setName("com.qvc.selenium.plugin.TestCaseFactory");
            classes.add(testClass);

            xmlTest.setXmlClasses(classes);

            Map <String, String> testParams = new HashMap<String, String>();

            testParams.put("suiteName", test.get("testfolder"));
            testParams.put("testName", testName);
            testParams.put("platform", test.get("os"));//testSet.get(i).get("PLATFORM"));
            testParams.put("client", test.get("client"));//testSet.get(i).get("CLIENT") );
            testParams.put("platformVersion", test.get("os"));//testSet.get(i).get("PLATFORMVERSION") );
            testParams.put("platformName",  test.get("os"));//testSet.get(i).get("PLATFORMNAME") );
            testParams.put("ui", test.get("country"));
            testParams.put("env", (String) config.get("env"));
            if(test.containsKey("grid")){//set specify grid value at TestCaseSet.xls - to control running on which Grid/PC
            	testParams.put("grid", test.get("grid"));
            }else{
            	testParams.put("grid", (String) config.get("grid"));
            }
            testParams.put("iteration", test.containsKey("iteration") ? test.get("iteration").trim() : "");
            testParams.put("testPackage", (String) config.get("testPackage"));
            testParams.put("appPath", test.get("app name"));
            testParams.put("deviceName", test.get("device name"));

            xmlTest.setParameters(testParams);

            logger.debug("built params");
        }

        logger.debug(suite.toXml());

        return suite;

    }

}