package com.mopub.nativeads.util;

import android.util.Log;

import com.mopub.nativeads.Constants;

public class Utils {
    public static void MoPubLog(String message) {
        MoPubLog(message, null);
    }

    public static void MoPubLog(String message, Exception exception) {
        Log.d(Constants.LOGTAG, message, exception);
    }
}
