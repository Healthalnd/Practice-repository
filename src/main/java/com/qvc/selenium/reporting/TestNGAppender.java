package com.qvc.selenium.reporting;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Reporter;

public class TestNGAppender extends AppenderSkeleton {
	public TestNGAppender(String name) {
		super();
		setName(name);
	}

	@Override
	protected void append(LoggingEvent event) {
		String logMessage = layout.format(event);
		if (!logMessage.isEmpty()) {
			Reporter.log(logMessage);
	
			String[] thorwableCrap = event.getThrowableStrRep();
	
			if (thorwableCrap != null) {
				Reporter.log(thorwableCrap.toString());
			}
		}
	}
    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
