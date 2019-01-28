// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;
import android.support.annotation.Nullable;

import com.mopub.common.AdReport;
import com.mopub.common.ExternalViewabilitySessionManager;
import com.mopub.common.logging.MoPubLog;

import java.util.Map;

import static com.mopub.common.DataKeys.AD_REPORT_KEY;
import static com.mopub.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.mopub.common.DataKeys.HTML_RESPONSE_BODY_KEY;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_SUCCESS;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;

public abstract class ResponseBodyInterstitial extends CustomEventInterstitial {
    public static final String ADAPTER_NAME = ResponseBodyInterstitial.class.getSimpleName();
    @Nullable
    private EventForwardingBroadcastReceiver mBroadcastReceiver;
    protected Context mContext;
    protected AdReport mAdReport;
    protected long mBroadcastIdentifier;
    protected ExternalViewabilitySessionManager mExternalViewabilitySessionManager;
    @Nullable protected Map<String, Object> mLocalExtras;

    abstract protected void extractExtras(Map<String, String> serverExtras);
    abstract protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener);
    public abstract void showInterstitial();

    @Override
    public void loadInterstitial(
            Context context,
            CustomEventInterstitialListener customEventInterstitialListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {
        MoPubLog.log(LOAD_ATTEMPTED, ADAPTER_NAME);

        mContext = context;
        mLocalExtras = localExtras;

        if (extrasAreValid(serverExtras)) {
            extractExtras(serverExtras);
        } else {
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME,
                    NETWORK_INVALID_STATE.getIntCode(),
                    NETWORK_INVALID_STATE);
            customEventInterstitialListener.onInterstitialFailed(NETWORK_INVALID_STATE);
            return;
        }


        try {
            mAdReport = (AdReport) localExtras.get(AD_REPORT_KEY);
            Long boxedBroadcastId = (Long) localExtras.get(BROADCAST_IDENTIFIER_KEY);
            if (boxedBroadcastId == null) {
                MoPubLog.log(CUSTOM, "Broadcast Identifier was not set in localExtras");
                MoPubLog.log(LOAD_FAILED, ADAPTER_NAME,
                        MoPubErrorCode.INTERNAL_ERROR.getIntCode(),
                        MoPubErrorCode.INTERNAL_ERROR);
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                return;
            }
            mBroadcastIdentifier = boxedBroadcastId;
        } catch (ClassCastException e) {
            MoPubLog.log(CUSTOM, "LocalExtras contained an incorrect type.");
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.INTERNAL_ERROR.getIntCode(),
                    MoPubErrorCode.INTERNAL_ERROR);
            customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
            return;
        }

        mBroadcastReceiver = new EventForwardingBroadcastReceiver(customEventInterstitialListener,
                mBroadcastIdentifier);
        mBroadcastReceiver.register(mBroadcastReceiver, context);

        preRenderHtml(customEventInterstitialListener);
        MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME);
    }

    @Override
    public void onInvalidate() {
        if (mBroadcastReceiver != null) {
            mBroadcastReceiver.unregister(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private boolean extrasAreValid(Map<String,String> serverExtras) {
        return serverExtras.containsKey(HTML_RESPONSE_BODY_KEY);
    }
}
