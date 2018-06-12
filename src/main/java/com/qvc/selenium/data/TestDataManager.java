package com.qvc.selenium.data;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.*;

public class TestDataManager {

	private static Logger logger = Logger.getLogger(TestDataManager.class);

	private static TestDataLoad <String, Properties> c = new TestDataLoad<String,Properties>() {
		@Override
		public Properties getData(String mapName) {
            logger.debug("In test data manager");
			return loadDataFile(mapName);
		}
	};

	private static TestDataLoad<String, Properties> dataCache = new TestDataLoadManager<String, Properties>(c);

	public static String getProperty(String dataName, String source) {
		String result = null;
		try {
			   result = dataCache.getData(source).getProperty(dataName);
		} catch (InterruptedException e) {
			logger.error("Interrupted Exception ", e);
		} catch(Exception e1)
        {
            logger.error("Exception ", e1);
        }

		return result;
	}

    public static void clearCache(){
        dataCache = new TestDataLoadManager<String, Properties>(c);
    }

	private static Properties loadDataFile(String mapName) {
			
		Properties prop = new Properties();
		logger.debug("Loading data file - " + mapName +  "testData.properties");	
		try {
			TestDataManager.class.getClassLoader();
			// load a properties file for items
			prop.load( new FileInputStream(mapName +  "testData.properties"));
			} catch (Exception e) {
				logger.error("error getting data", e);
			}
		return prop;
	}


    /**
     * get the test data by test case name  - add by Simon  10/23/2014
     * @param testName
     * @param testDataAll
     * @return current row's test data
     */
    public static HashMap<String,String> getTestData(String testName, ArrayList<HashMap<String, String>> testDataAll){
        boolean found = false;
        //loop test case list, if current testName match, return current row data.
        for(HashMap<String,String> row: testDataAll){
            if(row.get("TestName").equalsIgnoreCase(testName)){
                found = true;
                logger.debug("TestCase's data load, TestName = " + testName);
                return row;
            }
        }
        if(!found){
            logger.debug("TestCase Not Found, TestName = " + testName);
        }
        return null;
    }

    /**
     * get the test data by test case name  - add by Simon  10/23/2014
     * @param testName
     * @param testDataAll
     * @return current row's test data
     */
    public static ArrayList<HashMap<String,Object>> getTestDataList(String testName, ArrayList<HashMap<String, String>> testDataAll){
        boolean found = false;
        ArrayList<HashMap<String, Object>> testcaseDataList = new ArrayList<HashMap<String, Object>>();
        //loop test case list, if current testName match, return current row data.
        for(HashMap<String,String> row: testDataAll){
            if(row != null && row.containsKey("TestName") && row.get("TestName").equalsIgnoreCase(testName)){
                found = true;
                logger.debug("TestCase's data load, TestName = " + testName);
                HashMap<String, Object> testcaseData = new HashMap<String, Object>();
                for (String dataCol : row.keySet()) {
                    if (dataCol.equalsIgnoreCase("TestName"))
                            continue;
                    testcaseData.put(dataCol.toLowerCase(), row.get(dataCol));
                }
                testcaseDataList.add(testcaseData);
            }
        }
        if(!found){
            logger.debug("TestCase Not Found, TestName = " + testName);
        }
        return testcaseDataList;
    }


    /**
     * get all the env's test data and replace the productNum with actual value - add by Simon 11/13/2014
     * @param env
     * @param testDataHash
     * @return
     */
    public static ArrayList<HashMap<String, String>> getTestDataAll(String env, HashMap<String, ArrayList<HashMap<String, String>>> testDataHash){
        String productType = null;
        String newColumnValue = null;
        int beginIndex = 0;
        int endIndex = 0;

        ArrayList<HashMap<String, String>> testDataAll = testDataHash.get(env);
        ArrayList<HashMap<String, String>> products = testDataHash.get("PRODUCTS");

        for (HashMap<String, String> currentRow: testDataAll) {
            Set<String> keys = currentRow.keySet();
            for(String key: keys){
                String columnValue = currentRow.get(key);
                if(columnValue.startsWith("<<") && columnValue.endsWith(">>") ){ //e.g. <<Instock_NoColorSize>> - this used for ProductNum
                    beginIndex = 2;
                    endIndex = columnValue.length() - 2;
                    productType = columnValue.substring(beginIndex, endIndex);
                    newColumnValue = replaceWithActualValue(productType, products);
                    if(newColumnValue!=null){
                        //find the actual value, put to CellMap(it's will auto replace the old value)
                        currentRow.put(key, newColumnValue);
                        logger.debug("itemType: " + productType + ", use acutal item: " + newColumnValue + " for case - " + currentRow.get("TestName"));
                    }else{
                        logger.debug("itemType: " + productType + ", acutal item value was empty, please check the data sheet for case - " + currentRow.get("TestName"));
                    }

                }
            }

        }

        return testDataAll;

    }
    /**
     * replace the product type with actual value
     * @param itemType
     * @param products
     * @return a random value
     */
    private static String replaceWithActualValue(String itemType, ArrayList<HashMap<String, String>> products){
        String actulValue = null;
        Random ra =new Random();
        for(HashMap<String, String> currentRow: products){
            if(currentRow.get("ItemType").equalsIgnoreCase(itemType)){
                String [] values = currentRow.get("Values").split(";");
                int length = values.length;
                actulValue = values[ra.nextInt(length)];//use random to get a index, then get values array's value
                break;
            }
        }
        return actulValue;
    }

}


