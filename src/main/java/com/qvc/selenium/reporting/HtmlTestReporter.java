package com.qvc.selenium.reporting;

import com.qvc.selenium.drivers.PopupWindowReader;
import com.qvc.selenium.util.FormatTime;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HtmlTestReporter {
    private static final Logger logger = Logger.getLogger(HtmlTestReporter.class.getName());

    private static final String T_FORMAT = "HHmmssSSS";
    private static final String PASSED = "<span style='color:green;font-weight: bold'>Passed</span>";
    private static final String FAILED = "<span style='color:red;font-weight: bold'>Failed</span>";
    private String parentDir; // use to store screenshot,
    private String testCaseDir; // use to store screenshot,
    private StringBuffer htmlBody = new StringBuffer("");
    private StringBuffer htmlHeader = new StringBuffer("");
    private StringBuffer html = new StringBuffer("");
    private StringBuffer failedDetail = new StringBuffer("");
    private String executeStatus;
    private String executeDate;
    private String testCaseName;
    private int ExpModulesCount = 0;
    private int ActModulesCount = 0;
    private int failedSteps = 0;
    private int passedSteps = 0;
    private int total = 0;

    private String env;
    private String country;
    private String browser;
    private long start;
    private long end;
    private String elapsedTime;
    private boolean debugLevel = false;

    /**
     * Constructor
     */
    public HtmlTestReporter(String testCaseName, int steps, String env, String ui, String client, String execDate, boolean debugLevel) {
        this.start = System.currentTimeMillis();
        this.country = ui.toLowerCase();
        this.browser = client.toLowerCase();
        this.env = env.toLowerCase();

        this.executeDate = execDate;

        this.parentDir = Paths.get(System.getProperty("reportRootPath"), executeDate).toString();
        this.testCaseDir = Paths.get(parentDir, "DetailReport").toString(); // record the test case dir for screenshot and report file path
        this.testCaseName = testCaseName;
        this.ExpModulesCount = steps;

        this.debugLevel = debugLevel;
    }

    /**
     * generate the header info
     */
    public void appendHeader(HashMap<String, Object> testData) {
        executeStatus = "Not Complete";
        String data = null;

        String reportTitle = testCaseName + "_Report";
        String caseName = "<span style='color:black;font-weight: bold'>Test Case Name: </span><span style='color:blue'>" +
                testCaseName + "</span><br><br>\r\n";
        // Write some message into the file
        String title = "<!DOCTYPE html><html><head><title>" + reportTitle + "</title>" + "\r\n";
        String style = "<style>body {background-color: #cce8cf} table , th, td {border: 1px solid black; border-collapse: collapse;table-layout: fixed;} th, td {padding: 5px; text-align: left;word-wrap: break-word} tr{background-color:#eee;} .step{width: 5%} .status{width: 3%} .description{width: 25%} .comment{width: 20%} .page{width: 5%} .object{width: 20%} .inputs{width: 17%} .screenshot{width: 5%}</style></head>";
        String executionDate = "<span style='color:black;font-weight: bold'>Execution ID: </span><span>" + executeDate + "</span><br>\r\n";
        String env_browser = "<span style='color:black;font-weight: bold'>Execution Env: </span><span>" + country + " - " + env + " - " + browser + "</span><br>\r\n";
        String user = "<span style='color:black;font-weight: bold'>Executor: </span><span>" + System.getProperty("user.name") + "</span><br>\r\n";
        
        SummaryReporter.FailedData.put(this.testCaseName, testData.toString());
        
        data = "<span style='color:black;font-weight: bold'>TestData: </span><span>";
        if (testData != null) {
            data += testData.toString();
        } else {
            data += "Not set test data for this case at excel datasheet!";
        }
        data += "</span><br><br>\r\n";
        String log_link = "<a href=\"" + Paths.get("Attachments", testCaseName + ".log").toString() + "\">Log file</a><br><br>";

        String body_Start = "<body>\r\n";
        String detailSteps = "<h2 style='color:black;font-weight: bold'>Detail Steps: </h2>\r\n";

        htmlHeader.append(title);
        htmlHeader.append(style);
        htmlHeader.append(body_Start);
        htmlHeader.append(caseName);
        htmlHeader.append(executionDate);
        htmlHeader.append(env_browser);
        htmlHeader.append(user);
        htmlHeader.append(data);
        htmlHeader.append(log_link);
        htmlHeader.append(detailSteps);
    }

    /**
     * generate the Module -> Action info
     */
    public void appendModuleActionInfo(String action, String module) {
    	String moduleActionInfo = "<div><h3>" + module + " -> " + action + "</h3>";
        String tableTile = "<table><tbody>";
        String ColumnName = "<tr><th>Step</th><th>Status</th><th>Execute Description</th><th>Comment</th><th>Page</th><th>Object</th><th>Inputs</th><th>Screenshot</th></tr>";
        htmlBody.append(moduleActionInfo);
        htmlBody.append(tableTile);
        htmlBody.append(ColumnName);
        ActModulesCount++; // count for module steps
    }

    public void countEmptyModule(){
        ActModulesCount++; // count for module steps
    }

    /**
     * end the Module -> Action info
     */
    public void endModuleActionInfo() {
        String endTable = "</tbody></table></div>";
        htmlBody.append(endTable);

    }
    /**
     * append elapsed time to function
     */
    public void appendFunctionElapsedTime(String elapsedTime) {
        String elapsed_time = "<span style='color:blue;font-weight: bold'>" + elapsedTime + "</span><br><br>\r\n";
        htmlBody.append(elapsed_time);

    }

    /**
     * append result to report
     */
    public void appendResult(WebDriver driver, int step, ExecuteResult result, String comment, String page, String object, String inputs) {
        String status;
        String stepDetail;
        String content;
        String screenshot = "";

        stepDetail = result.getStepDetail();
        // get the result's status
        status = result.getStatus();
        if (status.equalsIgnoreCase("Passed")) {
            passedSteps++;
            if (debugLevel) {
//                String pageSource = formatXML(driver.getPageSource());
//                String pageSourceFileName = savePageSource(pageSource);
            	String pageSourceFileName = "";
                content = stepRow(PASSED, driver, step, stepDetail, comment, page, object, inputs, pageSourceFileName, null);
            } else{
                if(System.getProperty("TakeScreenShot").equalsIgnoreCase("Always"))
                	screenshot =  "<a href=\"" + takeScreentShot(driver) + "\">" + "ScreenShot"+"</a>";//take screen shot for each steps
                
            	content = "<tr><td class='step'>Step [" + step + "]</td>" + "<td class='status'>" + PASSED + "</td>" + "<td class='description'>" + stepDetail + "</td>" + "<td class='comment'>" + comment + "</td>" + "<td class='page'>" + page + "</td>" + "<td class='object'>" + object + "</td>" + "<td class='inputs'>" + inputs + "</td>" + "<td class='screenshot'>" + screenshot + "</td></tr>\r\n";
            	
            }
        } else {//Failed
            failedSteps++;
            String screenShotFileName = takeScreentShot(driver);
            String pageSource = formatXML(driver.getPageSource());
            logger.debug("Page source was:\n" + pageSource);
            String pageSourceFileName = savePageSource(pageSource);
            content = stepRow(FAILED, driver, step, stepDetail, comment, page, object, inputs, pageSourceFileName, screenShotFileName);
            // Add the failed step detail to reporter header
            failedDetail.append(content);
            if(!SummaryReporter.FailedComment.containsKey(this.testCaseName)){
            	SummaryReporter.FailedComment.put(this.testCaseName, comment);
            }
        }

        // Append to report.
        htmlBody.append(content);


    }

    /**
     * create report file
     */
    public void createReport() {
        this.completeReport(); // complete report first
        //
        if (ExpModulesCount == ActModulesCount) {// mean the test case run completed, then we can say it pass or fail
            if (failedSteps == 0) {
                executeStatus = "Pass";
            } else {
                executeStatus = "Fail";
            }

        } else {
            if (failedSteps == 0) {
                executeStatus = "NotComplete";
            } else {
                executeStatus = "Fail";
            }
        }
        String reportFile;
        if(System.getProperty("rerun") != null && System.getProperty("rerun").equalsIgnoreCase("yes")){
        	reportFile = Paths.get(this.testCaseDir, "ReRun", executeStatus + "_" + testCaseName + ".html").toString();
        	
        }else{
        	reportFile = Paths.get(this.testCaseDir, executeStatus + "_" + testCaseName + ".html").toString();
        	
        }

        // if the dir with a name of the test case name not exist, then create it
        File DirFile = new File(parentDir);
        if (!DirFile.exists()) {
            DirFile.mkdirs();
        }

        // create report file
        File reporte = new File(reportFile);
        if (!reporte.getParentFile().exists()) {
            reporte.getParentFile().mkdir();
        }
        try {
            reporte.createNewFile();
            logger.info("Report file generated: " + reporte.getAbsolutePath());
        } catch (IOException e) {
            logger.debug(e.getMessage());
            logger.debug("Create file failed,file name: " + reporte.getAbsolutePath());
        }

        this.appendElapsedTime();////add elapsed time to the report
        this.addFailedStepsInfoToHeader();//Add failed steps at the header

        html.append(htmlHeader);
        html.append(htmlBody);
        try {
            FileWriter writer = new FileWriter(reportFile, true);
            writer.write(html.toString());
            writer.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            logger.debug("Cannot write to report: " + reportFile);
        }

    }

    /**
     * Add failed test steps info to the header.
     */
    private void addFailedStepsInfoToHeader() {
        if (failedSteps > 0) {
            String failedHeader = "<div><h3>" + "Failed Steps: " + failedSteps + "</h3>";
            String tableTile = "<table><tbody>";
            String ColumnName = "<tr><th>Step</th><th>Status</th><th>Execute Description</th><th>Comment</th><th>Page</th><th>Object</th><th>Inputs</th><th>Screenshot</th></tr>";

            htmlHeader.append(failedHeader);
            htmlHeader.append(tableTile);
            htmlHeader.append(ColumnName);
            htmlHeader.append(failedDetail.toString());
            htmlHeader.append("</tbody></table></div><br><br>");// add end table fist for header
        }
    }

    /**
     *
     */
    public void completeReport() {
        String lastContent = "</body>\r\n</html>";
        htmlBody.append(lastContent);

    }

    /**
     * Get unique value, used for screen shot file name or else
     */
    private String getUniqueValue(String format) {
        String strValue;
        Date todayAndTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        strValue = sdf.format(todayAndTime);
        return strValue;
    }

    private String takeScreentShot(WebDriver driver) {
        File srcFile;
        String screenShotFullPath;
        
        String screenShotName = Paths.get("Attachments", testCaseName + "_" + getUniqueValue(T_FORMAT) + ".jpg").toString();
        if(System.getProperty("rerun") != null && System.getProperty("rerun").equalsIgnoreCase("yes"))
        	screenShotFullPath = Paths.get(this.testCaseDir, "ReRun", screenShotName).toString();
        else
        	screenShotFullPath = Paths.get(this.testCaseDir, screenShotName).toString();

		/* if run on local, the driver should be firefoxdriver or chromedriver or internetExplorerdriver or other..
         * if run on remote vm, the driver must be RemoteWebDriver */
        if (driver instanceof RemoteWebDriver) {
            srcFile = ((RemoteWebDriver) driver).getScreenshotAs(OutputType.FILE);
            logger.debug("Use RemoteWebDriver to take screenshot! - " + screenShotFullPath);
        } else {
            srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            logger.debug("Use Default WebDriver to take screenshot! - " + screenShotFullPath);
        }
        // store the picture.
        try {
            FileUtils.copyFile(srcFile, new File(screenShotFullPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return screenShotName;
    }

    /**
     * Add elapsed time to the summary report
     */
    private void appendElapsedTime() {
        end = System.currentTimeMillis();
        elapsedTime = FormatTime.formatTimeDuration(end - start);// get the test set elapsed time
        String elapsed_time = "<span style='color:black;font-weight: bold'>Elapsed Time: </span><span style='color:blue;font-weight: bold'>" + elapsedTime + "</span><br>\r\n";
        htmlHeader.append(elapsed_time);
    }

    private String stepRow(String status, WebDriver driver, int step, String stepDetail, String comment, String page, String object, String inputs, String pageSourceFileName, String screenShotFileName) {
        return "<tr>" +
                "<td class='step'>Step[" + step + "]</td>" +
                "<td class='status'>" + status + "</td>" +
                "<td class='description'>" + (stepDetail != null ? stepDetail : "") + "</td>" +
                "<td class='comment'>" + comment + "</td>" +
                "<td class='page'><a href=\"" + pageSourceFileName + "\">" + (page != null ? page : "current page") + "</a></td>" +
                "<td class='object'>" + (object != null ? object : "") + "</td>" +
                "<td class='inputs'>" + (inputs != null ? inputs : "") + "</td>" +
                "<td class='screenshot'>" + (screenShotFileName != null ? ("<a href=\"" + screenShotFileName + "\">" + "ScreenShot"+"</a>") : "") + "</td>" +
                "</tr>";
    }

    public void appendExceptionInfo(WebDriver driver, int step, ExecuteResult result, String comment, String page, String object, String inputs, String msg, String locator) throws IOException {
        failedSteps++;
        logger.debug("Locator was: " + locator);
        String screenShotFileName = takeScreentShot(driver);
        String pageSource = "";
        String pageSourceFileName = "Web empty";
//        if ((locator != null) && (locator.contains("//Window["))) {
//            String winName = locator.split("title,'")[1].split("'")[0];
//            pageSource = PopupWindowReader.getWinSource(winName);
//        } else
//            pageSource = formatXML(driver.getPageSource());
//        logger.debug("Page source was:\n" + pageSource);
//        pageSourceFileName = savePageSource(pageSource);
        if(!SummaryReporter.FailedComment.containsKey(this.testCaseName)){
        	SummaryReporter.FailedComment.put(this.testCaseName, comment);
        }
        String failRow = stepRow(FAILED, driver, step, result.getStepDetail(), comment, page, object + " <span style=\"color:green;\">" + locator + "</span>", inputs, pageSourceFileName, screenShotFileName);
        this.failedDetail.append(failRow);
        htmlBody.append(failRow);

        if (msg != null && !msg.isEmpty())
            msg += "<br><br>";

        String errorRow = "<tr><td colspan=\"8\">" +
                "<span style=\"color:red;\">" + msg + "</span>" +
                "<img height=\"800px\" src=\"" + screenShotFileName + "\">" +
                "</td></tr>";

        htmlBody.append(errorRow);
        executeStatus = "Fail";
    }

    private String savePageSource(String pageSource) {
        String xmlFN = Paths.get("Attachments", testCaseName + "_" + getUniqueValue(T_FORMAT) + ".xml").toString();
        String xmlFullPath = Paths.get(this.testCaseDir, xmlFN).toString();
        try {
            PrintStream out = new PrintStream(new FileOutputStream(xmlFullPath));
            out.print(pageSource);
            out.close();
        } catch (Exception e) {
            xmlFN = "null";
        }
        return xmlFN;
    }

    private String formatXML(String xmlContent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xmlContent));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
