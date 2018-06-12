package com.qvc.selenium.actions;

import org.apache.log4j.Logger;


public class ActionFactory {
	private static final Logger logger = Logger.getLogger(ActionFactory.class
			.getName());

	public static Action getAction(String actionType) {
		BaseAction action = null;
		logger.debug("Action Factory - " + actionType);
		switch (actionType.toUpperCase()) {
            case ("GOTOURL"): {
                action = new GoAction();
                break;
            }
            case ("MATH"):{
                action = new MathAction();
                break;
            }
            case ("CLICK"): {
                logger.debug("Clicking");
                action = new ClickAction();
                break;
            }
            case ("SENDKEYS"): {
                action = new SendKeysAction();
                break;
            }
            case ("CONTAINSTEXT"): {
                action = new VerifyContainsTextAction();
                break;
            }
            case ("NOTCONTAINSTEXT"): {
                action = new VerifyNotContainsTextAction();
                break;
            }
            case ("ELEMENTS"): {
                action = new NumberOfElementsStoreAction();
                break;
            }
            case ("CHECKFORELEMENTS"): {
                action = new VerifyContainsElementsAction();
                break;
            }
            case ("SELECT"): {
                action = new SelectAction();
                break;
            }
            case ("WEBTABLE"): {
                action = new WebTabletAction();
                break;
            }
            case ("TEXT"): {
                action = new TextStoreAction();
                break;
            }
            case ("WAITUNTILOBJECTEXIST"): {//WaitUntilObjectExist - Wait until the object show up to make sure we are switch to the correct page
                action = new WaitAction();
                break;
                }
            case ("FRAME"): {//take care the frame, switch in or switch out
                action = new FrameAction();
                break;
            }
            case ("CLEAR"): {
                action = new ClearTextAction();
                break;
            }
            case ("VERIFYNOTEXIST"): {//Verify object not exist
                action = new VerifyNonexistenceAction();
                break;
            }
            case ("VERIFYEXIST"): {//Verify object exist
                action = new VerifyContainsElementsAction();
                break;
            }
            case ("STARTSWITH"): {
                action = new VerifyStartsWithAction();
                break;
            }
            case ("MOUSEEVENT"): {//use for mouse event
                action = new MouseEventAction();
                break;
            }
            case ("BROWSER"): {//use for browser operation 
    			action = new BrowserAction();
    			break;
    		}
            case ("VERIFYOBJECTSTATUS"): {//verify object’s property status:e.g: verify button enabled or disabled, verify checkbox selected or not
    			action = new VerifyObjectStatusAction();
    			break;
    		}
            case ("BACK"): {
                action = new BackAction();
                break;
            }
            case ("HOME"): {
                action = new HomeAction();
                break;
            }
            case ("MENU"): {
                action = new OpenMenuAction();
                break;
            }
            case ("PINCH"): {
                action = new PinchAction();
                break;
            }
            case ("ZOOM"): {
                action = new ZoomAction();
                break;
            }
            case ("SCROLLTO"): {
                action = new ScrollAction();
                break;
            }
            case ("SWIPE"): {
                action = new SwipeAction();
                break;
            }
            case ("DRAGANDDROP"):{
                action = new DragAndDropAction();
                break;
            }
            case ("RESTARTAPP"):{
                action = new RestartAction();
                break;
            }
            case ("HIDEKEYBOARD"):{
                action = new HideKeyboardAction();
                break;
            }
            case ("ROTATE"):{
                action = new RotateAction();
                break;
            }
            case ("CHECKMAILBOX"):{
                action = new CheckMailBoxAction();
                break;
            }
            case ("TEXTEQUAL"):{
                action = new VerifyTextEqualAction();
                break;
            }
			   case ("VERIFYELEMENTSCOUNT"):{
                action = new VerifyElementsCount();
                break;
            }
            case ("VERIFYTIMEOFDAY"):{
            	action = new VerifyTimeOfDay();
            	break;
            }

            default:{
                logger.error("Action type \""+actionType+"\" was not recognized.");
                break;
            }
		}
		return action;
	}

}
