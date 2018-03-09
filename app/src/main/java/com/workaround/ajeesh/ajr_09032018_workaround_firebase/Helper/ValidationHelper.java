package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper
 * Created by ajesh on 09-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ValidationHelper {
    private static final String logName = "FIREB-HLPR-VALIDATION";
    private static final String domainName = "gmail.com";

    public boolean IsEmpty(String placeHolder) {
        boolean hasEmpty = placeHolder.equals("");
        LogHelper.LogThreadId(logName, "Place holder empty check is : " + hasEmpty);
        return hasEmpty;
    }

    public boolean isValidDomain(String Email) {
        String domain = Email.substring(Email.indexOf('@') + 1).toLowerCase();
        boolean check = domain.equals(domainName);
        LogHelper.LogThreadId(logName, "Place holder domain check is : " + check);
        return check;
    }

    public boolean doStringsMatch(String s1, String s2) {
        boolean check = s1.equals(s2);
        LogHelper.LogThreadId(logName, "Place holder string match check is : " + check);
        return check;
    }

}
