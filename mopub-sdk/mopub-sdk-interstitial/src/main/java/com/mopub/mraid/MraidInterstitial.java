// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mraid;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.CreativeOrientation;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.ResponseBodyInterstitial;

import java.util.Map;

import static com.mopub.common.DataKeys.CREATIVE_ORIENTATION_KEY;
import static com.mopub.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;

class MraidInterstitial extends ResponseBodyInterstitial {
    public static final String ADAPTER_NAME = MraidInterstitial.class.getSimpleName();
    @Nullable protected String mHtmlData;
    @Nullable private CreativeOrientation mOrientation;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mHtmlData = serverExtras.get(HTML_RESPONSE_BODY_KEY);
        mOrientation = CreativeOrientation.fromString(serverExtras.get(CREATIVE_ORIENTATION_KEY));
    }

    @Override
    protected void preRenderHtml(@NonNull CustomEventInterstitialListener
            customEventInterstitialListener) {
        MoPubLog.log(LOAD_ATTEMPTED, ADAPTER_NAME);
        MraidActivity.preRenderHtml(this, mContext, customEventInterstitialListener,
                mBroadcastIdentifier, mAdReport);
    }

    @Override
    public void showInterstitial() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        MraidActivity.start(mContext, mAdReport, mBroadcastIdentifier, mOrientation);
    }
}
