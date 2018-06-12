package com.qvc.selenium.actions;

import com.qvc.selenium.drivers.QVCAndroidDriver;
import com.qvc.selenium.drivers.QVCiOSDriver;
import com.qvc.selenium.reporting.ExecuteResult;


public class HideKeyboardAction extends BaseAction {

    @Override
    public ExecuteResult runAction() throws Exception {
        super.logAction();
        boolean res = true;
        String msg = "Hide keyboard";
        try {
            if (getDriver() instanceof QVCAndroidDriver) {
                try {
                    ((QVCAndroidDriver) getDriver()).hideKeyboard();
                } catch (Exception e) {
                    msg = "Soft keyboard not present";
                }
            } else if (getDriver() instanceof QVCiOSDriver) {
                ((QVCiOSDriver) getDriver()).hideKeyboard("tapOutside", "");
            }
        } catch (Exception e) {
            msg = "Can't hide keyboard. Original error: " + e.getMessage();
            res = false;
        }

        stepResult.setResult(res);
        stepResult.setStatus(res ? "Passed" : "Failed");
        stepResult.setStepDetail(msg);
        return stepResult;
    }
}
