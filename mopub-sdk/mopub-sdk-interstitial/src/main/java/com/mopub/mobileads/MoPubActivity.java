// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mopub.common.AdReport;
import com.mopub.common.CreativeOrientation;
import com.mopub.common.DataKeys;
import com.mopub.common.ExternalViewabilitySessionManager;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.DeviceUtils;
import com.mopub.mobileads.factories.HtmlInterstitialWebViewFactory;

import java.io.Serializable;

import static com.mopub.common.DataKeys.AD_REPORT_KEY;
import static com.mopub.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static com.mopub.common.DataKeys.CLICKTHROUGH_URL_KEY;
import static com.mopub.common.DataKeys.CREATIVE_ORIENTATION_KEY;
import static com.mopub.common.IntentActions.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.common.IntentActions.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.common.IntentActions.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.common.IntentActions.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.DID_DISAPPEAR;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.WILL_LEAVE_APPLICATION;
import static com.mopub.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_APPEAR;
import static com.mopub.common.util.JavaScriptWebViewCallbacks.WEB_VIEW_DID_CLOSE;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.broadcastAction;
import static com.mopub.mobileads.HtmlWebViewClient.MOPUB_FAIL_LOAD;
import static com.mopub.mobileads.HtmlWebViewClient.MOPUB_FINISH_LOAD;

public class MoPubActivity extends BaseInterstitialActivity {
    @Nullable private HtmlInterstitialWebView mHtmlInterstitialWebView;
    @Nullable private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

    public static void start(Context context, AdReport adReport, String clickthroughUrl,
                             CreativeOrientation creativeOrientation, long broadcastIdentifier) {
        MoPubLog.log(SHOW_ATTEMPTED);
        Intent intent = createIntent(context, adReport, clickthroughUrl,
                creativeOrientation, broadcastIdentifier);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Log.d("MoPubActivity", "MoPubActivity not found - did you declare it in AndroidManifest.xml?");
        }
    }

    static Intent createIntent(Context context,
                               AdReport adReport, String clickthroughUrl,
                               CreativeOrientation orientation, long broadcastIdentifier) {
        Intent intent = new Intent(context, MoPubActivity.class);
        intent.putExtra(CLICKTHROUGH_URL_KEY, clickthroughUrl);
        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        intent.putExtra(AD_REPORT_KEY, adReport);
        intent.putExtra(CREATIVE_ORIENTATION_KEY, orientation);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    static void preRenderHtml(final Interstitial baseInterstitial,
            final Context context,
            final AdReport adReport,
            final CustomEventInterstitialListener customEventInterstitialListener,
            final String clickthroughUrl,
            final long broadcastIdentifier) {
        MoPubLog.log(LOAD_ATTEMPTED);
        final HtmlInterstitialWebView htmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(
                context.getApplicationContext(), adReport, customEventInterstitialListener, clickthroughUrl);

        htmlInterstitialWebView.enablePlugins(false);
        htmlInterstitialWebView.enableJavascriptCaching();

        htmlInterstitialWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (MOPUB_FINISH_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialLoaded();
                } else if (MOPUB_FAIL_LOAD.equals(url)) {
                    customEventInterstitialListener.onInterstitialFailed(null);
                }

                return true;
            }
        });

        final ExternalViewabilitySessionManager externalViewabilitySessionManager =
                new ExternalViewabilitySessionManager(context);
        externalViewabilitySessionManager.createDisplaySession(context, htmlInterstitialWebView, true);

        htmlInterstitialWebView.loadHtmlResponse(getResponseString(adReport));
        WebViewCacheService.storeWebViewConfig(broadcastIdentifier, baseInterstitial,
                htmlInterstitialWebView, externalViewabilitySessionManager, null);
    }

    @Override
    public View getAdView() {
        Intent intent = getIntent();
        final String clickthroughUrl = intent.getStringExtra(CLICKTHROUGH_URL_KEY);
        final String htmlData = getResponseString();

        final Long broadcastIdentifier = getBroadcastIdentifier();
        if (broadcastIdentifier != null) {
            // If a cache hit happens, the content is already loaded; therefore, this re-initializes
            // the WebView with a new {@link BroadcastingInterstitialListener}, enables plugins,
            // and fires the impression tracker.
            final WebViewCacheService.Config config =
                    WebViewCacheService.popWebViewConfig(broadcastIdentifier);
            if (config != null && config.getWebView() instanceof HtmlInterstitialWebView) {
                mHtmlInterstitialWebView = (HtmlInterstitialWebView) config.getWebView();
                mHtmlInterstitialWebView.init(new BroadcastingInterstitialListener(), clickthroughUrl,
                        mAdReport != null ? mAdReport.getDspCreativeId() : null);
                mHtmlInterstitialWebView.enablePlugins(true);
                mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());

                mExternalViewabilitySessionManager = config.getViewabilityManager();

                return mHtmlInterstitialWebView;
            }
        }

        MoPubLog.log(CUSTOM, "WebView cache miss. Recreating the WebView.");
        mHtmlInterstitialWebView = HtmlInterstitialWebViewFactory.create(getApplicationContext(),
                mAdReport, new BroadcastingInterstitialListener(), clickthroughUrl);
        
        mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(this);
        mExternalViewabilitySessionManager.createDisplaySession(this, mHtmlInterstitialWebView, true);
        mHtmlInterstitialWebView.loadHtmlResponse(htmlData);
        return mHtmlInterstitialWebView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the device orientation
        Serializable orientationExtra = getIntent().getSerializableExtra(DataKeys.CREATIVE_ORIENTATION_KEY);
        CreativeOrientation requestedOrientation;
        if (orientationExtra == null || !(orientationExtra instanceof CreativeOrientation)) {
            requestedOrientation = CreativeOrientation.DEVICE;
        } else {
            requestedOrientation = (CreativeOrientation) orientationExtra;
        }
        DeviceUtils.lockOrientation(this, requestedOrientation);

        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.startDeferredDisplaySession(this);
        }
        broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_SHOW);
    }

    @Override
    protected void onDestroy() {
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.endDisplaySession();
            mExternalViewabilitySessionManager = null;
        }
        if (mHtmlInterstitialWebView != null) {
            mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_CLOSE.getUrl());
            mHtmlInterstitialWebView.destroy();
        }
        broadcastAction(this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_DISMISS);
        super.onDestroy();
    }

    class BroadcastingInterstitialListener implements CustomEventInterstitialListener {
        @Override
        public void onInterstitialLoaded() {
            MoPubLog.log(LOAD_SUCCESS);
            if (mHtmlInterstitialWebView != null) {
                mHtmlInterstitialWebView.loadUrl(WEB_VIEW_DID_APPEAR.getUrl());
            }
        }

        @Override
        public void onInterstitialFailed(MoPubErrorCode errorCode) {
            MoPubLog.log(LOAD_FAILED, MoPubErrorCode.VIDEO_CACHE_ERROR.getIntCode(),
                    MoPubErrorCode.VIDEO_CACHE_ERROR);
            broadcastAction(MoPubActivity.this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_FAIL);
            finish();
        }

        @Override
        public void onInterstitialShown() {
            MoPubLog.log(SHOW_SUCCESS);
        }

        @Override
        public void onInterstitialClicked() {
            MoPubLog.log(CLICKED);
            broadcastAction(MoPubActivity.this, getBroadcastIdentifier(), ACTION_INTERSTITIAL_CLICK);
        }

        @Override
        public void onInterstitialImpression() {
        }

        @Override
        public void onLeaveApplication() {
            MoPubLog.log(WILL_LEAVE_APPLICATION);
        }

        @Override
        public void onInterstitialDismissed() {
            MoPubLog.log(DID_DISAPPEAR);
        }
    }
}
