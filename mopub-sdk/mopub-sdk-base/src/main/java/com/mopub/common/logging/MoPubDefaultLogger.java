// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common.logging;

import android.support.annotation.Nullable;
import android.util.Log;

/**
 * All logs will be printed using android.util.Log.i(). As a result, filtering these by log level
 * is not possible.
 *
 * Due to the new format of the logs, filtering can be done by class or method name, or log event
 * message.
 */
public class MoPubDefaultLogger implements MoPubLogger {

    /**
     * MESSAGE_FORMAT is used to produce a log in the following format:
     * "[com.mopub.common.logging.MoPubLog][log] Ad Custom Log - Loading custom event adapter."
     */
    private static final String MESSAGE_FORMAT = "[%s][%s] %s";

    /**
     * MESSAGE_WITH_ID_FORMAT is used to produce a log in the following format:
     * "[com.mopub.common.logging.MoPubLog][log][ad-unit-id-123] Adapter Custom Log - Attempting to invoke custom event: com.mopub.mobileads.HtmlBanner"
     */
    private static final String MESSAGE_WITH_ID_FORMAT = "[%s][%s][%s] %s";

    public MoPubDefaultLogger() {
    }

    @Override
    public void log(@Nullable String className, @Nullable String methodName,
                    @Nullable String identifier, @Nullable String message) {
        if (identifier == null) {
            Log.i(MoPubLog.LOGTAG, String.format(MESSAGE_FORMAT, className,
                    methodName, message));
        } else {
            Log.i(MoPubLog.LOGTAG, String.format(MESSAGE_WITH_ID_FORMAT, className,
                    methodName, identifier, message));
        }
    }
}
