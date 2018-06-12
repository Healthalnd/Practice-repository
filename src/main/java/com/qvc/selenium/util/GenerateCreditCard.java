package com.qvc.selenium.util;

import org.apache.log4j.Logger;

public class GenerateCreditCard {
	static final Logger logger = Logger.getLogger(GenerateCreditCard.class
			.getName());
	public static void main(String[] args) {
		System.out.println(generateCC("VISA"));
		System.out.println(generateCC("MASTERCARD"));
//		System.out.println(generateCreditCard("AMERICAN EXPRESS"));
	}
	public static String generateCC(String cardType) {
		String prefix="";
		int length = 0;
		if(cardType == null || cardType == ""){
			logger.debug("You not set the cardType - cardType was null or empty");
		}
		switch (cardType.toLowerCase()){
		case "visa":
			length = 16;
			prefix = "4000";
			break;
		case "mastercard":
			length = 16;
			prefix = "5000";
			break;
		case "american express":
			length = 15;
			prefix = "3700";
			break;
		}
		StringBuilder prefixResult = new StringBuilder();
		prefixResult.append(prefix).append(String.format("%02d", 00
				+ (int) (Math.random() * 80)));
		//prefix +=String.format("%02d", "00"
		//		+ (int) (Math.random() * 80)).toString();
		
		String ccnumber = prefixResult.toString();

		// generate digits

		while (ccnumber.length() < (length - 1)) {
			ccnumber += new Double(Math.floor(Math.random() * 10)).intValue();
		}

		// reverse number and convert to int

		//String reversedCCnumberString = strrev(ccnumber);
			String reversedCCnumberString = "";
			for (int i = ccnumber.length() - 1; i >= 0; i--) {
				reversedCCnumberString += ccnumber.charAt(i);
			}

		
		/*
		 * List <Integer> reversedCCnumberList = new Vector <Integer>()); for
		 * (int i = 0; i < reversedCCnumberString.length(); i++) {
		 * reversedCCnumberList.add(new Integer(String
		 * .valueOf(reversedCCnumberString.charAt(i)))); }
		 */

		int[] reversedCCnumber = new int[16];
		for (int i = 0; i < reversedCCnumberString.length(); i++) {
			reversedCCnumber[i] = Integer.parseInt(String
					.valueOf(reversedCCnumberString.charAt(i)));
		}

		// calculate sum

		int sum = 0;
		int pos = 0;

		//Integer[] reversedCCnumber = reversedCCnumberList
		//		.toArray(new Integer[reversedCCnumberList.size()]);
		while (pos < length - 1) {

			int odd = reversedCCnumber[pos] * 2;
			if (odd > 9) {
				odd -= 9;
			}

			sum += odd;

			if (pos != (length - 2)) {
				sum += reversedCCnumber[pos + 1];
			}
			pos += 2;
		}

		// calculate check digit

		int checkdigit = new Double(
				((Math.floor(sum / 10) + 1) * 10 - sum) % 10).intValue();
		ccnumber += checkdigit;

		return ccnumber;
	}
}
