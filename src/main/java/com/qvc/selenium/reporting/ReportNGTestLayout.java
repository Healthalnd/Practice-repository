package com.qvc.selenium.reporting;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.commons.lang3.StringEscapeUtils;

public class ReportNGTestLayout extends PatternLayout {
	public ConcurrentHashMap<String, String> threadTestName = new ConcurrentHashMap<String, String>();
	public int consoleLevel = Level.DEBUG_INT;
	public String reportParentFolder = "test-output";
	public String executionDate;
	
	@Override
	public String format(LoggingEvent event) {

		String logline = "";
		String logFileName;
		String logPrefix;
		String consolePrefix;
		// Get test name by thread id
		String testName = threadTestName.get(Long.toString(Thread.currentThread().getId()));
		String curThreadName = Thread.currentThread().getName();
		
		if (testName == null) {
			// log to a file by thread name, if no test name
			consolePrefix = logPrefix = " " + curThreadName + " : ";
			logFileName = Paths.get(reportParentFolder, "main.log").toString();
		} else {
			// log to <test name>.log file
			if(System.getProperty("rerun") != null && System.getProperty("rerun").equalsIgnoreCase("yes"))
				logFileName = Paths.get(reportParentFolder, "DetailReport", "ReRun", "Attachments", testName + ".log").toString();
			else
				logFileName = Paths.get(reportParentFolder, "DetailReport", "Attachments", testName + ".log").toString();
			
			logPrefix = " : ";			
			if (!curThreadName.contains("pool") && !curThreadName.equalsIgnoreCase("TestNG"))
				logPrefix = logPrefix + "[" + curThreadName + "] ";
			consolePrefix = testName + logPrefix;
		}
			
		// Console output
		if (event.getLevel().toInt() >= consoleLevel) {
			if (event.getLevel().toInt() > consoleLevel)
				consolePrefix += event.getLevel() + " : ";
			String consoleMessage = new SimpleDateFormat("HH:mm:ss.SSS ").format(new Date(event.timeStamp))
					+ consolePrefix + event.getMessage();
			if (event.getLevel().toInt() >= Level.INFO_INT)
				consoleMessage = "\n" + consoleMessage;
			System.out.println(consoleMessage);
		}
		
		if (!reportParentFolder.isEmpty()){
			// Create log file if needed
			File logFile = new File(logFileName);
			if (!logFile.exists()){
				try {
					File parentFolder = new File(logFile.getParent());
					parentFolder.mkdirs();
					logFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Append to log file
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)))) {
			    out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
			    					.format(new Date(event.timeStamp)) +
						" [" + event.getLevel() + "]" +
						logPrefix +
						event.getMessage());
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
		
		// Append to old report if INFO message
		if (event.getLevel().toInt() == Level.INFO_INT) {
			
			String newMsg = StringEscapeUtils.escapeHtml3(event.getMessage()
					.toString());
			Throwable t = null;
			ThrowableInformation ti = event.getThrowableInformation();
			if (ti != null) {
				t = ti.getThrowable();
			}

			LoggingEvent encodedEvent = new LoggingEvent(
					event.fqnOfCategoryClass, Logger.getLogger(event
							.getLoggerName()), event.timeStamp,
					event.getLevel(), newMsg, t);

			String baseFmt = super.format(encodedEvent).replace("@{{", "<")
					.replace("@}}", ">");
			logline = "<div class=step" + "event.level.toString()" + ">"
					+ baseFmt + "</div><br/>";
			
		}
		
		return logline;
	}
}
