package com.github.aleksandrsavosh.simplestore;

import android.util.Log;

public class LogUtil {

    private static final String TAG = "SIMPLE_STORE";
    private static boolean isUseLog = false;

    public static void setIsUseLog(boolean isUseLog) {
        LogUtil.isUseLog = isUseLog;
    }

    public static void toLog(String message){
        if(isUseLog){
            Log.i(TAG, message);
        }
    }

    public static void toLog(String message, Exception e){
        if(isUseLog){
            message = e.getMessage() == null?message:e.getMessage();
            Log.e(TAG, message);
            Log.d(TAG, message, e);
        }
    }

}
