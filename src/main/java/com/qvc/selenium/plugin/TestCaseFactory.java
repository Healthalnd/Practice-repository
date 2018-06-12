package com.qvc.selenium.plugin;

import com.qvc.selenium.data.TestDataManager;
import com.qvc.selenium.data.TestManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class TestCaseFactory {

    private static final Logger logger = Logger.getLogger(TestCase.class.getName());

    protected RemoteWebDriver driver;

    protected WebDriverWait wait;

    @Parameters({"suiteName", "testName","platform", "client", "platformVersion", "platformName", "ui", "grid", "env", "testPackage","appPath", "deviceName", "iteration"})
    @Factory
    public Object[] createInstance(
            String suiteName,
            String testName,
            String platform,
            String client,
            String platformName,
            String platformVersion,
            String ui,
            String grid,
            String env,
            String testPackage,
            String appPath,
            String deviceName,
            String iteration) throws Exception {
        logger.debug("Factory createInstance");
        TestCase[] testCaseArray = null;

        String testFile = Paths.get(testPackage, "tests", suiteName, testName.replaceAll("_"+ui+"_"+client,"")).toString();
        if (!Paths.get(testFile + ".xlsx").toFile().exists()){//when test file under country's folder, add this country to Path, Simon, 5/20/2016
        	testFile = Paths.get(testPackage, "tests",ui, suiteName, testName.replaceAll("_"+ui+"_"+client,"")).toString();
        }
        HashMap<String, ArrayList<HashMap<String, String>>> testFlowHash = TestManager.getTestXSLFile(testFile);

        int caseCount = 1;
        int repeat = 0;

        if (testFlowHash.get("TEST") == null) {
            logger.error(String.format("File \"%s\" does not contains \"TEST\" sheet", testFile));
            caseCount = 0;
            return new TestCase[caseCount];
        }

        //get the data sheet and replace the productNum with actual value
        String testDataPathWithName = Paths.get(testPackage, "data", ui, ui.toUpperCase() + "_Ecom_Datasheet_" + env.toUpperCase()).toString();
        HashMap<String, ArrayList<HashMap<String, String>>> testDataHash = TestManager.getTestXSLFile(testDataPathWithName);//e.g.. \\data\\us\\US Ecom Datasheet_QA
        ArrayList<HashMap<String, String>> testDataAll = TestDataManager.getTestDataAll(env.toUpperCase(), testDataHash); // get all the env's test data and replace the productNum with actual value
        ArrayList<HashMap<String, Object>> testDataList = TestDataManager.getTestDataList(testName.replaceAll("_"+ui+"_"+client,""), testDataAll);

        if (testDataList.size() > caseCount)
            caseCount = testDataList.size();
        
        if (!iteration.isEmpty()){
        	repeat = Integer.valueOf(iteration).intValue();
        	caseCount = repeat;
        }
        
        testCaseArray = new TestCase[caseCount];
        String testNameIR;
        logger.debug("Building Test Case Array - length: " + caseCount);
        for(int i = 0; i < caseCount; i++){
            ArrayList<HashMap<String, String>> testFlow = testFlowHash.get("TEST");
            testCaseArray[i] = new TestCase();
            testCaseArray[i].setTestFlow(testFlow);
            testCaseArray[i].setPlatform(platform);
            testCaseArray[i].setBrowserType(client);
            testCaseArray[i].setTargetTestServer(grid);
            testCaseArray[i].setUi(ui);
            testCaseArray[i].setEnv(env);
            testCaseArray[i].setTestPackage(testPackage);
            testCaseArray[i].setSuiteName(suiteName);
            testCaseArray[i].setAppPath(appPath);
            testCaseArray[i].setDeviceName(deviceName);
            if (i < testDataList.size()){
            	testCaseArray[i].setTestData(testDataList.get(i));
            }else if(repeat > 1){//repeat run - use the same data
            	testCaseArray[i].setTestData(testDataList.get(0));
            }
            
            if(i >= 1){
            	testNameIR = testName + "_Iteration" + (i+1);
            	testCaseArray[i].setTestCaseName(testNameIR);
            	testNameIR = "";//reset the value to empty
            }else
            	testCaseArray[i].setTestCaseName(testName);
        }
        logger.debug("Factory Create Instance End");

        return testCaseArray;
    }

}