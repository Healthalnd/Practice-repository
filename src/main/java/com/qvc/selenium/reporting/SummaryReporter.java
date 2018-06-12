package com.qvc.selenium.reporting;


import com.qvc.selenium.data.TestManager;
import com.qvc.selenium.util.FormatTime;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.*;

public class SummaryReporter {
    private static final Logger logger = Logger.getLogger(SummaryReporter.class.getName());

    public static final String PASSED = "<span style='color:green;font-weight: bold'>Passed</span>";
    public static final String FAILED = "<span style='color:red;font-weight: bold'>Failed</span>";
    public static final String ERROR = "<span style='color:red;font-weight: bold'>Error | Connection</span>";
    public static final String NOTCOMPLETE = "<span style='color:gray;font-weight: bold'>Not Completed</span>";
    
    public static HashMap<String, String> FailedComment = new HashMap<String, String>();
    public static HashMap<String, String> FailedData = new HashMap<String, String>();
    
    private String parentDir;
    private String detailReportPath;
    private String reportName;
    private String executeDate;
    private String executeTime;
    private StringBuffer htmlBody = new StringBuffer("");
    private StringBuffer htmlHeader = new StringBuffer("");
    private StringBuffer html = new StringBuffer("");
    private ArrayList<HashMap<String, String>> caseStatusList;
    
    private String env;
    private String executeSet;
    private long start;
    private long end;
    private String elapsedTime;
    private String reportNameWoDate;
    private String reportRootPath;
    
    public SummaryReporter(String execDate, String testSuite) {
        reportRootPath = System.getProperty("reportRootPath");
        start = System.currentTimeMillis();
        executeSet = testSuite;
        this.reportNameWoDate = "SummaryReport";
        this.parentDir = Paths.get(reportRootPath, execDate).toString();
        this.executeTime = getUniqueValue("yyyy-MM-dd HH:mm:ss");
        this.detailReportPath = Paths.get(reportRootPath, execDate, "DetailReport").toString();
        this.executeDate = execDate;
        if(System.getProperty("rerun") != null && System.getProperty("rerun").equalsIgnoreCase("yes"))
        	this.reportName = executeSet + "_" + reportNameWoDate + "_ReRun";
        else
        	this.reportName = executeSet + "_" + reportNameWoDate;
    }


    /**
     * create report file
     */
    public void createReport() {
        //
        String reportFile = Paths.get(this.parentDir, reportName + ".html").toString();

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
            logger.info("Summary report file generate: " + reportFile);
        } catch (IOException e) {
            logger.info("Create summary report file failed, file name: " + reportFile);
        }
        //generate html report
        this.appendHeader();
        ArrayList<HashMap<String, String>> statusList;
        if(System.getProperty("rerun") != null && System.getProperty("rerun").equalsIgnoreCase("yes")){
        	statusList = this.scanDetailReport(true);//scan Detail -> ReRun folder first
        	statusList = this.scanDetailReport(false);//scan Detail folder
        }else{
        	statusList = this.scanDetailReport(false);//scan Detail folder
        }
        	
        this.appendBody(statusList);
        this.appendElapsedTime();//add elapsed time to the report
        html.append(htmlHeader);
        html.append(htmlBody);
        try {
            FileWriter writer = new FileWriter(reportFile, true);
            writer.write(html.toString());
            writer.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            logger.debug("Cannot write to report: " + parentDir);
        }

        this.createExcelForReRun(statusList);
    }

    /**
     * generate the header info
     */
    public void appendHeader() {

        String reportTitle = reportName + "_Report";
        String caseName = "<span style='color:black;font-weight: bold'>Report Name: </span><span style='color:blue'>" +
                reportName + "</span><br><br>\r\n";
        // Write some message into the file
        String title = "<!DOCTYPE html><html><head><title>" + reportTitle + "</title>" + "\r\n";
        String style = "<style>table, th, td {border: 1px solid black; border-collapse: collapse;} th, td {padding: 5px; text-align: left;} tr{background-color:#eee;}</style></head>";
        String body_Start = "<body>\r\n";
        String executionDate = "<span style='color:black;font-weight: bold'>Execution Date: </span><span>" + executeTime + "</span><br>\r\n";
        String user = "<span style='color:black;font-weight: bold'>Executor: </span><span>" + System.getProperty("user.name") + "</span><br>\r\n";
        String mainLogFile = "<span><a href=\"main.log\">Main Log</a></span><br>\n";
        htmlHeader.append(title);
        htmlHeader.append(style);
        htmlHeader.append(body_Start);
        htmlHeader.append(caseName);
        htmlHeader.append(executionDate);
        htmlHeader.append(user);
        htmlHeader.append(mainLogFile);

    }

    /**
     * Use scan results for html report body
     */
    public void appendBody(ArrayList<HashMap<String, String>> testCaseList) {
    	String testSetName = "TestCaseSet";
        if(System.getProperty("jenkins.buildNumber") == null){
        	testSetName = "TestCaseSetTest";
        }
    	
    	String tableTile = "<div><table><tbody>";
        //String ColumnName = "<tr><th>TestName</th><th>Country</th><th>Client</th><th>Status</th><th>Report</th><th>Log</th><th>ExecutionTime</th><th>ReRunFlag</th></tr>";
        String ColumnName = "<tr><th>Module</th><th>Function</th><th>TestName</th><th>Country</th><th>Status</th><th>Comment</th><th>Data</th><th>Client</th><th>Report</th><th>Log</th></tr>";

        htmlBody.append(tableTile);
        htmlBody.append(ColumnName);
        int passCount = 0;
        int errorCount = 0;
        int failCount = 0;
        int notCompleteCount = 0;
        int total;
        String testPackage = System.getProperty("testPackage");
        HashMap<String, ArrayList<HashMap<String, String>>> testSetHash = null;;
        ArrayList<HashMap<String, String>> testSet = null;
        try{
        	testSetHash = TestManager.getTestXSLFile(Paths.get(testPackage, "tests", testSetName).toString());
        	testSet = testSetHash.get(executeSet.toUpperCase());
        }catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("Load TestCaseSet.xls file failed - " + e1.getMessage() );
		}
        
        for (HashMap<String, String> testcase : testCaseList) {
            String testName = testcase.get("TestName");
            String status = testcase.get("Status");
            String report = testcase.get("ReportPath");
            String logger = testcase.get("LogFile");
            String country = testcase.get("Country");
            String client = testcase.get("Client");
            String executionTime = testcase.get("ExecutionTime");
            String reRunFlag = testcase.get("ReRun");
            HashMap<String, String> test = getTestCase(testSet, testName, country, client);
            String module = "";
            if(test.containsKey("Module") && test.get("Module")!=null){
            	module = test.get("Module");
            }
            String function = "";
            if(test.containsKey("Function") && test.get("Function")!=null){
            	function = test.get("Function");
            }

            String TestNameSuffix = "_" + country + "_" + client;
            String comment = "";
            if(FailedComment.containsKey(testName+TestNameSuffix)){
            	comment = "Failed Step: " + FailedComment.get(testName+TestNameSuffix);
            }
            String data = FailedData.get(testName+TestNameSuffix);
            try {
				data = ExecuteRex(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
            report = "<a href=\"" + report + "\">Detail Report</a>";
            logger = "<a href=\"" + logger + "\">Log file</a>";
            String content;
            if (status.equalsIgnoreCase("Error")) {
                errorCount++;
                //content = "<tr><td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + client + "</td>" + "<td>" + ERROR + "</td>" + "<td>Not created</td>" + "<td>" + logger + "</td>" + "<td>" + executionTime + "</td>" + "<td>" + reRunFlag + "</td></tr>\r\n";
                content = "<tr><td>" + module + "</td>" +"<td>" + function + "</td>" +"<td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + ERROR + "</td>" + "<td>" + comment + "</td>" + "<td>" + data + "</td>" + "<td>" + client + "</td>" + "<td>Not created</td>" + "<td>" + logger + "</td></tr>\r\n";
            } else if (status.equalsIgnoreCase("Passed")) {
            	//for Passed test cases, no need to add data and comment(failed steps), so make them empty
            	data="";
            	comment = "";//set it to empty, to make sure when re-run Passed, this column was empty
            	passCount++;
                //content = "<tr><td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + client + "</td>" + "<td>" + PASSED + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td>" + "<td>" + executionTime + "</td>" + "<td>" + reRunFlag + "</td></tr>\r\n";
                content = "<tr><td>" + module + "</td>" +"<td>" + function + "</td>" +"<td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + PASSED + "</td>" + "<td>" + comment + "</td>" + "<td>" + data + "</td>" + "<td>" + client + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td></tr>\r\n";
            } else if (status.equalsIgnoreCase("Failed")) {
                failCount++;
                //content = "<tr><td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + client + "</td>" + "<td>" + FAILED + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td>" + "<td>" + executionTime + "</td>" + "<td>" + reRunFlag + "</td></tr>\r\n";
                content = "<tr><td>" + module + "</td>" +"<td>" + function + "</td>" +"<td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + FAILED + "</td>" + "<td>" + comment + "</td>" + "<td>" + data + "</td>" + "<td>" + client + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td></tr>\r\n";
            } else { // "not competed" status
                notCompleteCount++;
                //content = "<tr><td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + client + "</td>" + "<td>" + NOTCOMPLETE + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td>" + "<td>" + executionTime + "</td>" + "<td>" + reRunFlag + "</td></tr>\r\n";
                content = "<tr><td>" + module + "</td>" +"<td>" + function + "</td>" +"<td>" + testName + "</td>" + "<td>" + country + "</td>" + "<td>" + NOTCOMPLETE + "</td>" + "<td>" + comment + "</td>" + "<td>" + data + "</td>" + "<td>" + client + "</td>" + "<td>" + report + "</td>" + "<td>" + logger + "</td></tr>\r\n";
            }

            htmlBody.append(content);

        }

        htmlBody.append("</tbody></table></div><br><br></body>\r\n</html>");
        //Add the execution summary to the header - how many pass, how many fail...
        total = passCount + failCount + notCompleteCount + errorCount;
        String summary = "<h3 style='color:black;font-weight: bold'>" + executeSet + " - Total: " + total + "- Passed: " + passCount + ", Failed: " + failCount + ", Not Competed: " + notCompleteCount + ", Error | Connection: " + errorCount + "</h3>\r\n";
        htmlHeader.append(summary);

    }

    /**
     * Scan the DetailReport folder:
     * -> Check the ReRun folder first, if exist add to
     * -> Check the DetailReport folder
     * -> Check the Attachment folder add the log file
     * -> if no report file found mark case status as error
     * Update by Simon, 8/23/2016
     */
    public ArrayList<HashMap<String, String>> scanDetailReport(boolean rerun) {
    	File testSet = null;
    	File testSetAttachments;
    	String reRunFlag;
    	if(caseStatusList == null)
    		caseStatusList = new ArrayList<>();
    		
    		
    	if(rerun){//Check ReRun folder, if exist add to the caseStatsusList first - Simon 8/22/2016
    		testSet = new File(Paths.get(detailReportPath, "ReRun").toString());
    		testSetAttachments = new File(Paths.get(detailReportPath, "ReRun", "Attachments").toString());
    	}else{
    		testSet = new File(detailReportPath);
    		testSetAttachments = new File(Paths.get(detailReportPath, "Attachments").toString());
    	}
    	
        if(testSet.exists()){
        	File[] reportFiles = testSet.listFiles();
            logger.debug("The execute set exist, all the test case files has been returned. - " + detailReportPath);
            
        	 for (File reportFile : reportFiles) {
                 String testName = reportFile.getName();
                 if (testName.endsWith("html")) {
                     HashMap<String, String> rowDetail = new HashMap<>();
                     testName = deleteFileType(testName);
                     String status = getFileStatus(testName);//get the execution status
                     String path = getFilePath(reportFile);//get the report path
                     String time = getExecutionTime(reportFile);//get the execution time
                     if(rerun)
                    	 reRunFlag = "Yes";//rerun cases report all under ReRun folder this flag was "Yes"
                     else
                    	 reRunFlag = "No";//1st run - detail report all underdetailReportPath, so the flag was "NO"
                     String client = getWordFromEnd(testName, 1);
                     String country = getWordFromEnd(testName, 2);
                     String onlyTestName = getOnlyTestName(testName, 1, 2);
                     
                     rowDetail.put("FullTestName", testName);
                     rowDetail.put("TestName", onlyTestName);
                     rowDetail.put("Status", status);
                     rowDetail.put("ReportPath", path);
                     rowDetail.put("ExecutionTime", time);
                     rowDetail.put("ReRun", reRunFlag);
                     rowDetail.put("Client", client);
                     rowDetail.put("Country", country);
                     
                   //we may rerun failed and not completed cases, so when add to caseStatusList, need to check first
                   //if rerun results already added to caseStatusList, no need to add again, otherwise add it
                 	if(caseStatusList == null || !isCaseExist(caseStatusList, onlyTestName, client, country)){
                 		caseStatusList.add(rowDetail);
                 	}
                 }
             }
       
            //add log file's info
//            File testSetAttachments = new File(Paths.get(detailReportPath, "Attachments").toString());
            if (testSetAttachments.exists())
                reportFiles = testSetAttachments.listFiles();

            for (File reportFile : reportFiles) {
                String fileName = reportFile.getName();
                if (fileName.endsWith("log")) {
                    boolean testError = true;
                    for (HashMap<String, String> rowDetail : caseStatusList) {
                        if (rowDetail.get("FullTestName") != null && rowDetail.get("FullTestName").contains(deleteFileType(fileName))) {
                        	if(!rowDetail.containsKey("LogFile")){//if LogFile not add, add it
                        		rowDetail.put("LogFile", getFilePath(reportFile));
                        		testError = false;
                        		break;
                        	
                        	}else if(rowDetail.containsKey("ReRun") && rowDetail.get("ReRun").equalsIgnoreCase("Yes")){
                        		//Skip - because ReRun log file already add
                        		testError = false;
                        		break;
                        	}
                        		
                        }

                    }
                    if (testError) {
                        HashMap<String, String> rowDetail = new HashMap<>();
                        String time = getExecutionTime(reportFile);//get the execution time
                        String testName = deleteFileType(fileName);
                        String onyTestName = getOnlyTestName(testName, 0, 2);
                        String client = getWordFromEnd(testName, 1);
                        String country = getWordFromEnd(testName, 2);
                        rowDetail.put("ExecutionTime", time);
                        rowDetail.put("TestName", onyTestName);
                        rowDetail.put("Status", "Error");
                        rowDetail.put("LogFile", getFilePath(reportFile));
                        rowDetail.put("Client", client);
                        rowDetail.put("Country", country);
                        rowDetail.put("ReRun", "No");
                        //no need to add again, if already add
                        if(caseStatusList == null || !isCaseExist(caseStatusList, onyTestName, client, country)){
                     		caseStatusList.add(rowDetail);
                     	}
                    }
                }
            }
        } else {
            logger.debug("The execute set does not exist! - " + detailReportPath);
        }

        return caseStatusList;

    }

    
    /**
     * After re-run, case may already added to caseStatusList
     */
	private boolean isCaseExist(
			ArrayList<HashMap<String, String>> caseStatusList, String testName, String client, String country) {

		boolean exist = false;
		for (HashMap<String, String> caseStatus : caseStatusList) {
			if (caseStatus.get("TestName").equals(testName) && caseStatus.get("Client").equals(client) && caseStatus.get("Country").equals(country)) {
				exist = true;
				break;
			}
		}

		return exist;
	}

	/**
     * Get report file's relative path 
     */
    private String getFilePath(File file) {
        String fileName = file.getName();
        boolean rerun = false;
        if(file.getPath().toLowerCase().contains("rerun"))
        	rerun = true;
        
        if (fileName.endsWith("log")){
        	if(rerun)
        		return Paths.get("DetailReport", "ReRun", "Attachments", fileName).toString();
        	else
        		return Paths.get("DetailReport", "Attachments", fileName).toString();
        	
        }
        else{
        	if(rerun)
        		return Paths.get("DetailReport","ReRun", fileName).toString();
        	else
        		return Paths.get("DetailReport", fileName).toString();
        }
    }

    private String deleteFileType(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * Get report file's lastModified time - use for: test case execution time
     */
    private String getExecutionTime(File file) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = file.lastModified();
        cal.setTimeInMillis(time);
        logger.debug("FileModify:" + formatter.format(cal.getTime()));
        return formatter.format(cal.getTime());
    }

    /**
     * Get report file's status - Passed or Failed
     */
    private String getFileStatus(String reportName) {
        String status;
        String[] aryStatus = reportName.split("_");
        String realStatus = aryStatus[0].toLowerCase();
        if (realStatus.contains("pass")) {
            status = "Passed";
        } else if (realStatus.contains("fail")) {
            status = "Failed";
        } else {
            status = "Not Completed";
        }
        return status;
    }


    private String getWordFromEnd(String reportName, int number) {
    	String[] aryStatus = reportName.split("_");
    	//if test name end with Iteration
    	if(aryStatus[aryStatus.length - 1].startsWith("Iteration")){
    		number++;
    	}
        return aryStatus[aryStatus.length - number];
    }

    private String getOnlyTestName(String reportName, int startIndex, int wordCountToRemove) {
    	boolean iteration = false;
        String[] aryStatus = reportName.split("_");
        //if test name end with Iteration
    	if(aryStatus[aryStatus.length - 1].startsWith("Iteration")){
    		wordCountToRemove++;
    		iteration = true;
    	}
        StringBuilder testName = new StringBuilder();
        testName.append(aryStatus[startIndex]);
        for (int index=startIndex+1; index < aryStatus.length - wordCountToRemove; index++){
            testName.append("_").append(aryStatus[index]);
        }
        
        if(iteration)//e.g. add the Iteration2 to the test name
        	return testName.append("_").append(aryStatus[aryStatus.length - 1]).toString();
        else
        	return testName.toString();
        	
    }

    /**
     * @param detail_time
     * @param summary_time
     * @return reRunFlag - YES or NO
     */
    private String getReRunFlag(String detail_time, String summary_time) {
        String reRunFlag = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dtime = null;
        Date stime = null;
        try {
            dtime = formatter.parse(detail_time);
            stime = formatter.parse(summary_time);
            if (dtime.before(stime)) {
                reRunFlag = "No";
            } else {
                reRunFlag = "Yes";
            }
        } catch (ParseException e) {
            logger.debug("parse date fail, please check the code at SummaryReporter.java -> line 280");
        }


        return reRunFlag;
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

    /**
     * Add elapsed time to the summary report
     */
    private void appendElapsedTime() {
        end = System.currentTimeMillis();
        elapsedTime = FormatTime.formatTimeDuration(end - start);// get the test set elapsed time
        String elapsed_time = "<span style='color:black;font-weight: bold'>Elapsed Time: </span><span style='color:blue;font-weight: bold'>" + elapsedTime + "</span><br>\r\n";
        htmlHeader.append(elapsed_time);
    }

    /**
     * generate a excel file to store the failed and not completed test cases - use for re-run
     */
    private void createExcelForReRun(ArrayList<HashMap<String, String>> testCaseList) {
    	String testSetName = "TestCaseSet";
        if(System.getProperty("jenkins.buildNumber") == null){
        	testSetName = "TestCaseSetTest";
        }
    	Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
        Sheet sheet = wb.createSheet(executeSet);

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow((short) 0);
        // Create a cell and put a value in it.- set the column name
        row.createCell((short) 0).setCellValue("TestName");
        row.createCell((short) 1).setCellValue("TestFolder");
        row.createCell((short) 2).setCellValue("Country");
        row.createCell((short) 3).setCellValue("OS");
        row.createCell((short) 4).setCellValue("Client");
        row.createCell((short) 5).setCellValue("Device Name");
        row.createCell((short) 6).setCellValue("App Name");
        row.createCell((short) 7).setCellValue("Disable");
        row.createCell((short) 8).setCellValue("Status");
        row.createCell((short) 9).setCellValue("ExecutionTime");
        row.createCell((short) 10).setCellValue("Grid");
        
        String testPackage = System.getProperty("testPackage");
        HashMap<String, ArrayList<HashMap<String, String>>> testSetHash = null;;
        ArrayList<HashMap<String, String>> testSet = null;
        //1. loading the TestCaseSet file
    	try {
			testSetHash = TestManager.getTestXSLFile(Paths.get(testPackage, "tests", testSetName).toString());
			testSet = testSetHash.get(executeSet.toUpperCase());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("Load TestCaseSet.xls file failed - " + e1.getMessage() );
		}
        int index = 1;
        //2. write the test case info to re-run excel
        for (int i = 0; i < testCaseList.size(); i++) {
            row = sheet.createRow(index);
            HashMap<String, String> curRowData = testCaseList.get(i);
            String status = curRowData.get("Status");
            if (!status.equalsIgnoreCase("Passed")) {//when have failed and not completed test case
                String testName = curRowData.get("TestName");
                String country = curRowData.get("Country");
                String client = curRowData.get("Client");
                HashMap<String, String> test = getTestCase(testSet, testName, country, client);
                if(test != null){
                	row.createCell((short) 0).setCellValue(testName);
                	row.createCell((short) 1).setCellValue(test.get("TestFolder"));
                	row.createCell((short) 2).setCellValue(test.get("Country"));
                	row.createCell((short) 3).setCellValue(test.get("OS"));
                	row.createCell((short) 4).setCellValue(test.get("Client"));
                	row.createCell((short) 5).setCellValue(test.get("Device Name"));
                	row.createCell((short) 6).setCellValue(test.get("App Name"));
//                row.createCell((short) 7).setCellValue("");Disable can set to "Y"
                	row.createCell((short) 8).setCellValue(status);
                	row.createCell((short) 9).setCellValue(curRowData.get("ExecutionTime"));
                	row.createCell((short) 10).setCellValue(test.get("Grid"));
                	index++;
                	
                }
            }
        }

        if (index == 1) {
            logger.debug("Test cases all running passed, no need to create re-run(NotPassed) file");
            System.setProperty("RunningStatus", "AllPassed");
            wb = null;
        } else {
            System.setProperty("RunningStatus", "NotCompleted");//set this flag for auto rerun once
            // Write to a file
        	String path = Paths.get(reportRootPath, "NotPassed").toString();
        	// if the NotPassed dir not exist, then create it
        	File DirFile = new File(path); 
        	if(!DirFile.exists()){
        		DirFile.mkdirs();
        	}
            String filename = Paths.get(path,("NotPassed_" + executeSet + ".xlsx")).toString();
            try {
                FileOutputStream fileOut = new FileOutputStream(filename);
                wb.write(fileOut);
                fileOut.close();
                logger.debug("Re-run(NotPassed) file generated - " + filename);
            } catch (FileNotFoundException e) {
                logger.debug(e.getMessage());
            } catch (IOException ioE) {
                logger.debug(ioE.getMessage());
                logger.debug("Can't write to excel report: " + filename);
            }

        }


    }
    
    /**
	 * 
	 * @param setList
	 * @param testCaseName
	 * @return test case
	 */
    private HashMap<String, String> getTestCase(ArrayList<HashMap<String, String>> setList, String testCaseName, String country, String client){
		HashMap<String, String> testcase = null;
		boolean iteration = testCaseName.contains("Iteration");
		if(!iteration){
			for(HashMap<String, String> curRow: setList){
				if(curRow.get("TestName").equalsIgnoreCase(testCaseName) && curRow.get("Country").equalsIgnoreCase(country) && curRow.get("Client").equalsIgnoreCase(client)){
					testcase = curRow;
					break;
				}
			}
			
		}
		if(!iteration && testcase == null)
			logger.error("Not found the failed test case in TestCaseSet file - " + testCaseName);
		
		return testcase;
	}
    
    //The function is used for filter the data, just return the product number and test email/pin.
    private static String ExecuteRex(String data) throws Exception{
		String productNumberString = "";
		String emailString = "";
		String pinString="";
		Pattern productNumber = Pattern.compile("productnum\\d?=.*?,");
		Pattern email = Pattern.compile("email=.*?,");
		Pattern pin = Pattern.compile("pin=.*?,");
		Matcher m = productNumber.matcher(data);
		while(m.find()){
			productNumberString = productNumberString + m.group() + "\n";
		}
		m = email.matcher(data);
		if(m.find()){
			emailString = m.group();
		}
		m = pin.matcher(data);
		if(m.find()){
			pinString = m.group();
		}
		data = productNumberString + emailString+ "\n" + pinString;

		return data;
	}
	
}

