package com.mopub.network;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.Preconditions;
import com.mopub.mobileads.MoPubError;

import static android.os.SystemClock.uptimeMillis;

class ContentDownloadAnalytics {
    enum DownloadResult {
        AD_LOADED("ad_loaded"),
        MISSING_ADAPTER("missing_adapter"),
        TIMEOUT("timeout"),
        INVALID_DATA("invalid_data");

        @NonNull
        private final String value;

        DownloadResult(@NonNull String loadResult) {
            value = loadResult;
        }
    }

    private static final String LOAD_DURATION_MS_MACRO = "%%LOAD_DURATION_MS%%";
    private static final String LOAD_RESULT_MACRO = "%%LOAD_RESULT%%";

    @Nullable
    Long mBeforeLoadTime = null;
    @NonNull
    private AdResponse mAdResponse;

    ContentDownloadAnalytics(@NonNull AdResponse adResponse) {
        Preconditions.checkNotNull(adResponse);

        mAdResponse = adResponse;
    }

    void reportBeforeLoad(@Nullable Context context) {
        if (context == null) {
            return;
        }

        String url = mAdResponse.getBeforeLoadUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        mBeforeLoadTime = uptimeMillis();
        makeHttpRequest(url, context);
    }

    void reportAfterLoad(@Nullable Context context, @Nullable final MoPubError errorCode) {
        if (context == null || mBeforeLoadTime == null) {
            return;
        }

        DownloadResult result = errorCodeToDownloadResult(errorCode);
        String url = generateAfterLoadUrl(mAdResponse.getAfterLoadUrl(), result.value);
        if (TextUtils.isEmpty(url)) {
            return;
        }

        makeHttpRequest(url, context);
    }

    @Nullable
    private String generateAfterLoadUrl(@Nullable String url, @NonNull String loadResult) {
        if (TextUtils.isEmpty(url) || mBeforeLoadTime == null) {
            return null;
        }

        if (!url.contains(LOAD_DURATION_MS_MACRO) || !url.contains(LOAD_RESULT_MACRO)) {
            return null;
        }

        url = url.replace(LOAD_DURATION_MS_MACRO, String.valueOf(uptimeMillis() - mBeforeLoadTime));
        return url.replace(LOAD_RESULT_MACRO, Uri.encode(loadResult));
    }

    private void makeHttpRequest(@Nullable final String url, @Nullable final Context context) {
        TrackingRequest.makeTrackingHttpRequest(url, context);
    }

        @NonNull
    private DownloadResult errorCodeToDownloadResult(@Nullable final MoPubError errorCode) {
        if (null == errorCode) {
            return DownloadResult.AD_LOADED;
        }

        switch (errorCode.getIntCode()) {
            case MoPubError.ER_SUCCESS:
                return DownloadResult.AD_LOADED;
            case MoPubError.ER_TIMEOUT:
                return DownloadResult.TIMEOUT;
            case MoPubError.ER_ADAPTER_NOT_FOUND:
                return DownloadResult.MISSING_ADAPTER;
        }

        return DownloadResult.INVALID_DATA;
    }
}

