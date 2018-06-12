package com.qvc.selenium.data;

import org.apache.http.annotation.ThreadSafe;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ThreadSafe
public class PageObjectManager {

    private static Logger logger = Logger.getLogger(PageObjectManager.class);
    protected static StoredValueDataManager data = StoredValueDataManager
            .getInstance();

    private static TestDataLoad<String, PageObject> c = new TestDataLoad<String, PageObject>() {
        @Override
        public PageObject getData(String type) {
            return loadPage(type);
        }
    };

    private static TestDataLoad<String, PageObject> objectCache = new TestDataLoadManager<String, PageObject>(
            c);

    public static PageObject getPage(String pageName, String ui, String client) throws InterruptedException {

        pageName = pageName + ";" + ui + ";" + client;
        PageObject result = null;
        try {
            logger.debug("getting from cache  " + pageName);
            result = objectCache.getData(pageName);
        } catch (InterruptedException e) {
            logger.debug(e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("returning " + result);
        return result;
    }

    public static void clearCache(){
        objectCache = new TestDataLoadManager<String, PageObject>(c);
    }

    /**
     * Load PageObject from specified file
     *
     * @param page File with locators for objects on a page
     * @return
     */
    protected static PageObject loadPage(String page) {
        String ui = null;
        String client = null;
        if (page.contains(";")){
            String[] pageAttr = page.split(";");
            client = pageAttr[pageAttr.length-1];
            ui = pageAttr[pageAttr.length-2];
            page = pageAttr[0];
        }
        logger.debug("Load Page Object - " + page);
        if (page.endsWith(".xlsx")) {
            try {
                return loadPageFromXlsx(page, ui, client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return loadPageFromXML(page);
        }
        return null;
    }

    /**
     * Gets PageObject from .xlsx file
     *
     * @param pageFile File with locators for objects on a page
     * @return PageObject with locators for controls on the page
     * @throws Exception
     */
    private static PageObject loadPageFromXlsx(String pageFile, String ui, String client) throws Exception {
        HashMap<String, ArrayList<HashMap<String, String>>> res = new HashMap<>();

        InputStream inp = new FileInputStream(pageFile);
        Workbook wb = new XSSFWorkbook(inp);

        Sheet sheet = wb.getSheetAt(0);
        List<Integer> curColIndex = getCurrentColIndexes(sheet, ui + "_" + client);
        int rowCount = sheet.getPhysicalNumberOfRows();
        for (int rowIndex = 1; rowIndex <= rowCount - 1; rowIndex++) {

            Row row = sheet.getRow(rowIndex);
            if (row == null || row.getCell(0) == null)
                continue;

            String objectName = row.getCell(0).toString();

            ArrayList<HashMap<String, String>> properties = new ArrayList<>();

            for (int cellIndex : curColIndex) {
                Cell cell = row.getCell(cellIndex);
                if (cell != null) {
                    HashMap<String, String> propMap = getPropMap(cell);
                    if (propMap.size() > 0)
                        properties.add(propMap);
                }
            }
            if (properties.size() == 0) {
                String msg = "Can't take locators for PageObject '" + objectName + "' from '" + pageFile + "'";
                logger.debug(msg);
            }
            res.put(objectName, properties);
        }

        PageObject currPage = new PageObject(pageFile);
        currPage.setProperties(res);

        inp.close();

        return currPage;
    }

    private static List<Integer> getCurrentColIndexes(Sheet sheet, String currentTitle) {
        List<Integer> res = new ArrayList<>();
        Row row = sheet.getRow(0);
        int colCount = row.getPhysicalNumberOfCells();
        for (int cellIndex = 1; cellIndex <= colCount - 1; cellIndex++) {
            if (currentTitle.equalsIgnoreCase(row.getCell(cellIndex).toString())) {
                res.add(cellIndex);
            }
        }
        return res;
    }

    private static HashMap<String, String> getPropMap(Cell cell) {
        HashMap<String, String> res = new HashMap<>();
        String content = cell.toString();
        String key;
        String value;
        if (content.length() > 0 && !content.isEmpty() && !content.equals("-")) {
            if (content.contains("=")) {
                key = content.split("=")[0];
                value = content.replaceFirst(key + "=", "");
            } else {
                key = "label";
                value = content;
            }
            res.put(key, value);
        }
        return res;
    }

    protected static PageObject loadPageFromXML(String page) {

        logger.debug("Load Page Object - " + page);
        PageObject currPage = new PageObject(page);

        Document pageDoc = getXMLDoc(page);
        String[] includes = pageDoc.getDocumentElement().getAttribute("include").split(";");

        HashMap<String, ArrayList<HashMap<String, String>>> topMap = new HashMap<String, ArrayList<HashMap<String, String>>>();

        // Get all the object nodes
        NodeList testObjects = pageDoc.getElementsByTagName("TestObject");

        for (int i = 0; i < testObjects.getLength(); i++) {
            // get the name of the test object

            Element testObject = (Element) testObjects.item(i);
            String nodeName = testObject.getAttribute("name");

            topMap.put(nodeName, parseXML(testObject));
            logger.debug("No exceptions yet...1");

        }
        // Add include data
        if (includes[0].length() > 0) {
            logger.debug("No exceptions yet...3 --- Include size = " + includes.length);
            for (int x = 0; x < includes.length; x++) {

                Document includeDoc = getXMLDoc(includes[x]);
                NodeList includeObjects = includeDoc
                        .getElementsByTagName("TestObject");

                for (int i = 0; i < includeObjects.getLength(); i++) {
                    // get the name of the test object
                    Element testObject = (Element) includeObjects.item(i);
                    String nodeName = testObject.getAttribute("name");

                    topMap.put(nodeName, parseXML(testObject));

                }
            }
        }
        logger.debug("No exceptions yet...2");
        currPage.setProperties(topMap);
        logger.debug("how bout now?");
        return currPage;
    }

    /**
     * Update: add code to get the "env" value, then decide to load the expect env's properties - Simon 11/18/2014
     *
     * @param testObject
     * @return
     */
    public static ArrayList<HashMap<String, String>> parseXML(Element testObject) {
        ArrayList<HashMap<String, String>> middleArray = new ArrayList<HashMap<String, String>>();
        NodeList properties = testObject.getChildNodes();
        String expEnv = System.getProperty("env");//get the actual running environment -e.g.. qa, int or dev
        boolean loadProperties = false;
        for (int j = 0; j < properties.getLength(); j++) {

            NodeList hashElements = properties.item(j).getChildNodes();
            for (int x = 0; x < hashElements.getLength(); x++) {

                if (!hashElements.item(x).getNodeName().equalsIgnoreCase("#text")) {
                    //add code to get the "env" value, then decide to load the expect env's properties
                    Element property = (Element) properties.item(j);
                    String env = property.getAttribute("env");
                    if (env == "" || env == null) {//this is a common object that for each environment
                        loadProperties = true;
                    } else if (env.equalsIgnoreCase(expEnv)) {//this is a environment specified object
                        loadProperties = true;
                        logger.debug("load " + env + " environment's properties.");
                    } else {//the "env" value was another environment
                        loadProperties = false;
                        logger.debug("Env - " + env + "'s object properties will not load!");
                    }

                    if (loadProperties) {//load the properties
                        HashMap<String, String> bottomMap = new HashMap<String, String>();
                        bottomMap.put(hashElements.item(x).getNodeName(),
                                hashElements.item(x).getTextContent());
                        logger.debug(hashElements.item(x).getNodeName() + " : "
                                + hashElements.item(x).getTextContent());
                        middleArray.add(bottomMap);
                    }
                }
            }

        }
        return middleArray;
    }

    public static Document getXMLDoc(String page) {
        // Step 1: create a DocumentBuilderFactory and setNamespaceAware
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        // Step 2: create a DocumentBuilder
        DocumentBuilder db;
        Document doc = null;

        try {
            db = dbf.newDocumentBuilder();
            String currentPageFile = page;
            // Step 3: parse the input file to get a Document object
            if(!page.endsWith(".xml")){
	           	//When take care the include xml files, need set it with path - update by Simon Zhang, 1028/2015
	            String testPackage = (String) data.getStoredData("testPackage");
	            currentPageFile = Paths.get(testPackage, "pageObjects", (String)data.getStoredData("ui"), page + ".xml").toString();
            }
            doc = db.parse(currentPageFile);
        } catch (SAXException | IOException e) {
            // TODO add Log 4 J
            logger.debug("File not found! please check your page name - page: " + page);
            logger.fatal(e);
        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            logger.fatal(e1);
        }
        return doc;
    }

}
