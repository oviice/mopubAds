package com.mopub.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;

public class IntentUtils {
    private static final String TWITTER_APPLICATION_DEEPLINK_URL = "twitter://timeline";
    private static final String PLAY_GOOGLE_COM = "play.google.com";
    private static final String MARKET_ANDROID_COM = "market.android.com";
    private static final String MARKET = "market";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private IntentUtils() {}

    public static boolean deviceCanHandleIntent(final Context context, final Intent intent) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            return !activities.isEmpty();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isHttpUrl(final String url) {
        if (url == null) {
            return false;
        }

        final String scheme = Uri.parse(url).getScheme();
        return (HTTP.equals(scheme) || HTTPS.equals(scheme));
    }

    private static boolean isAppStoreUrl(final String url) {
        if (url == null) {
            return false;
        }

        final Uri uri = Uri.parse(url);
        final String scheme = uri.getScheme();
        final String host = uri.getHost();

        if (PLAY_GOOGLE_COM.equals(host) || MARKET_ANDROID_COM.equals(host)) {
            return true;
        }

        if (MARKET.equals(scheme)) {
            return true;
        }

        return false;
    }

    public static boolean isDeepLink(final String url) {
        return isAppStoreUrl(url) || !isHttpUrl(url);
    }

    public static boolean canHandleTwitterUrl(Context context) {
        return canHandleApplicationUrl(context, TWITTER_APPLICATION_DEEPLINK_URL, false);
    }

    public static boolean canHandleApplicationUrl(Context context, String url) {
        return canHandleApplicationUrl(context, url, true);
    }

    public static boolean canHandleApplicationUrl(Context context, String url, boolean logError) {
        // Determine which activities can handle the intent
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        // If there are no relevant activities, don't follow the link
        if (!IntentUtils.deviceCanHandleIntent(context, intent)) {
            if (logError) {
                Log.w("MoPub", "Could not handle application specific action: " + url + ". " +
                        "You may be running in the emulator or another device which does not " +
                        "have the required application.");
            }
            return false;
        }

        return true;
    }
}
