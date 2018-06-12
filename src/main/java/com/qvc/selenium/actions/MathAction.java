package com.qvc.selenium.actions;
import com.qvc.selenium.reporting.ExecuteResult;

import java.util.HashMap;

import org.apache.log4j.Logger;

/*This action is used to:
Do some math arithmetic of the number.
Operation can be: 1.plus 2.minus 3.multiply 4.divide 5.equal 6.greater
Add by Theresa    03/13/2017
 * */
public class MathAction extends BaseAction {
	
	@Override
	public ExecuteResult runAction() throws Exception {
		boolean result = false;
		String status = null;
		String stepDetail = null;
		super.logAction();
		ExecuteResult stepResult = new ExecuteResult();
		double NumberLeft = Double.parseDouble(HandleString((String) testData.get("numberleft")));// the number is user for arithmetic on the left
		double NumberRight = Double.parseDouble(HandleString((String) testData.get("numberright")));// the number is user for arithmetic on the right
		String Operation = (String) testData.get("operation"); // operation is used for switch to different arithmetic
		double FinalResultNum = 0; //the result of arithmetic will store in it 
		String FinalResult = "";
		try{
			switch (Operation.toLowerCase()){
			case "plus":{  //Do the plus math arithmetic
				try{
				FinalResultNum = NumberLeft + NumberRight;
				FinalResult = String.format("%.2f", FinalResultNum);
				}catch (Exception e){
					logger.debug(e.getMessage());
					result = false;
					status = "Failed";
					stepDetail = "Math Action: Plus two variable failed:  " + NumberLeft +" and " + NumberRight;
					logger.debug("Math Action: Plus two variable failed:  " + NumberLeft +" and " + NumberRight);
					break;
				}
				result = true;
				status = "Passed";
				stepDetail = "Math Action: Plus two variable pass:  " + NumberLeft +" plus " + NumberRight +" ,the result is " + FinalResult;
				logger.debug("Math Action: Plus two variable pass:  " + NumberLeft +" plus " + NumberRight +" ,the result is " + FinalResult);
				break;
			}
			case "minus":{ //Do the minus math arithmetic
				try{
					FinalResultNum = NumberLeft - NumberRight;
					FinalResult = String.format("%.2f", FinalResultNum);
					}catch (Exception e){
						logger.debug(e.getMessage());
						result = false;
						status = "Failed";
						stepDetail = "Math Action: Minus two variable failed:  " + NumberLeft +" and " + NumberRight;
						logger.debug("Math Action: Minus two variable failed:  " + NumberLeft +" and " + NumberRight);
						break;
					}
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Minus two variable pass:  " + NumberLeft +" minus " + NumberRight +" ,the result is " + FinalResult;
					logger.debug("Math Action: Minus two variable pass:  " + NumberLeft +" minus " + NumberRight +" ,the result is " + FinalResult);
					break;
			}
			
			case "multiply":{//Do the multiply math arithmetic
				try{
					FinalResultNum = NumberLeft * NumberRight;
					FinalResult = String.format("%.2f", FinalResultNum);
					}catch (Exception e){
						logger.debug(e.getMessage());
						result = false;
						status = "Failed";
						stepDetail = "Math Action: Multiply two variable failed:  " + NumberLeft +" and " + NumberRight;
						logger.debug("Math Action: Multiply two variable failed:  " + NumberLeft +" and " + NumberRight);
						break;
					}
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Multiply two variable pass:  " + NumberLeft +" multiply " + NumberRight +" ,the result is " + FinalResult;
					logger.debug("Math Action: Multiply two variable pass:  " + NumberLeft +" multiply " + NumberRight +" ,the result is " + FinalResult);
					break;
			}
			case "divide":{ //Do the divide math arithmetic
				try{
					FinalResultNum = NumberLeft/NumberRight;
					FinalResult = String.format("%.2f", FinalResultNum);
					}catch (Exception e){
						logger.debug(e.getMessage());
						result = false;
						status = "Failed";
						stepDetail = "Math Action: Divide two variable failed:  " + NumberLeft +" and " + NumberRight;
						logger.debug("Math Action: Divide two variable failed:  " + NumberLeft +" and " + NumberRight);
						break;
					}
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Divide two variable pass:  " + NumberLeft +" divide " + NumberRight +" ,the result is " + FinalResult;
					logger.debug("Math Action: Divide two variable pass:  " + NumberLeft +" divide " + NumberRight +" ,the result is " + FinalResult);
					break;
			}
			case "greaterorequal":{//Compare two number, if number on left greater or equal than number on the right or not
				if (NumberLeft>=NumberRight) {
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Compare two variable pass:  " + NumberLeft +" is greater or equal than " + NumberRight;
					logger.debug("Math Action: Compare two variable pass:  " + NumberLeft +" is greater or equal than " + NumberRight);
					
				} else {
					result = false;
					status = "Failed";
					stepDetail = "Math Action: Compare two variable failed:  " + NumberLeft +" is not greater or equal than " + NumberRight;
					logger.debug("Math Action: Compare two variable failed:  " + NumberLeft +" is not greater or equal than " + NumberRight);
				}
				break;
			}
			case "greater":{//Compare two number, if number on left greater than number on the right or not
				if (NumberLeft>NumberRight) {
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Compare two variable pass:  " + NumberLeft +" is greater than " + NumberRight;
					logger.debug("Math Action: Compare two variable pass:  " + NumberLeft +" is greater than " + NumberRight);
					
				} else {
					result = false;
					status = "Failed";
					stepDetail = "Math Action: Compare two variable failed:  " + NumberLeft +" is not greater than " + NumberRight;
					logger.debug("Math Action: Compare two variable failed:  " + NumberLeft +" is not greater than " + NumberRight);
				}
				break;
			}
			case "equal":{//Compare two number/string is equal or not.
				String string1 = (String) testData.get("numberleft");
				String string2 = (String) testData.get("numberright");
				
				if (NumberLeft==NumberRight) {//Compare two number
					result = true;
					status = "Passed";
					stepDetail = "Math Action: Compare two variable pass:  " + NumberLeft +" is equal to " + NumberRight;
					logger.debug("Math Action: Compare two variable pass:  " + NumberLeft +" is equal to " + NumberRight);	
				} else if (string1.equals(string2)){//Compare two string
					result = true;
					status = "Passed";
					stepDetail = "Compare two string pass:  " + string1 +" is equal to " + string2;
					logger.debug("Compare two string pass:  " + string1 +" is equal to " + string2);	
				} else{
					result = false;
					status = "Failed";
					stepDetail = "Math Action: Compare two variable failed:  " + NumberLeft +" is not equal to " + NumberRight;
					logger.debug("Math Action: Compare two variable failed:  " + NumberLeft +" is not equal to " + NumberRight);
				}
				break;
			}
			default:
				result = false;
				status = "Failed";
				stepDetail = "Math operation failed - no vaule set for mathaction";
				logger.debug("Math operation failed - no vaule set for mathaction");
			}
			HashMap<String, Object> curTestCaseData = (HashMap<String, Object>) data.getStoredData("testCaseData");
	        curTestCaseData.put("mathresult", String.valueOf(FinalResult));
	        data.setStoredData("testCaseData", curTestCaseData);
		}catch (Exception e) {
			// TODO: handle exception
			logger.debug(e.getMessage());
		}
		stepResult.setResult(result);
		stepResult.setStatus(status);
		stepResult.setStepDetail(stepDetail);
		return stepResult;
	}
	
	public String HandleString(String Number){//Deal with the string to cut the money symbol of "$/£/€"
		Number =  Number.replace("$", "");
		Number =  Number.replace("£", "");
		Number =  Number.replace("€", "");
		
		//For de, the number is contains symbol  "," in the price, such as: € 697,00 , so should deal with it. Add by Kerry, 12/6/2017 
		String country = (String)data.getStoredData("ui");
		if(country.equalsIgnoreCase("de")){
			Number = Number.replaceAll("\\,", "\\.");
		}
		
		return Number;
	}
}
