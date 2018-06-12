package com.qvc.selenium.util;

public class FormatTime {
	
	public  static String formatTimeDuration(long duration) {
		//millisecond
		long msec = duration % 1000;
		// seconds
		long sec = (duration / 1000) % 60;
		// minutes
		long min = (duration / 1000 / 60) % 60;
		// hour
		long hour = (duration / 1000 / 60 / 60) % 24;
		// day
		long day = duration / 1000 / 60 / 60 / 24;

		if (day > 0) {
			return day + " day, " + hour + " hour, " + min + " minutes, " + sec + " seconds";
		}

		if (hour > 0) {
			return hour + " hour, " + min + " minutes, " + sec + " seconds";
		}

		if (min > 0) {
			return min + " minutes, " + sec + " seconds";
		}
		
		if (sec > 0 ){
			return sec + " seconds";	
		}
		
		return msec + " millisecond";
	}
}
