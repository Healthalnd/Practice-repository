package com.qvc.selenium.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateNewAccount {

	
	public static String generateEmailAddress(){
        String newEmailAddress;
        Date date = new Date();
        DateFormat format = new SimpleDateFormat(
                "yyyyMMddhhmmss");
        newEmailAddress = "a" + format.format(date) + "@qvc.com";
        return newEmailAddress;

    }

    public static String generatePassword(){
        String newPassword;
        Date date = new Date();
        DateFormat format = new SimpleDateFormat(
                "yyyyMMddhhmmss");
        newPassword = "a" + format.format(date) + "qvcPas";
        return newPassword;

    }

    public static String generateSecurityAnswer(){
        String securityAnswer;
        Date date = new Date();
        DateFormat format = new SimpleDateFormat(
                "yyyyMMddhhmmss");
        securityAnswer = "a" + format.format(date) + "answer";
        return securityAnswer;

    }
    
    public static String generateHouseNumber(){
    	String houseNumber;
    	houseNumber = String.format("%02d", 00
				+ (int) (Math.random() * 9999));
    	return houseNumber;
    } 
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(generateEmailAddress());
	}
	
}
