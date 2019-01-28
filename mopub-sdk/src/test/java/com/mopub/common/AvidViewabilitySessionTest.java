// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;

import com.integralads.avid.library.mopub.video.AvidVideoPlaybackListener;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.test.support.ShadowAvidAdSessionManager;
import com.mopub.common.util.test.support.ShadowAvidManager;
import com.mopub.common.util.test.support.ShadowReflection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SdkTestRunner.class)
@Config(shadows = {ShadowAvidAdSessionManager.class,
        ShadowAvidManager.class, ShadowReflection.class})
public class AvidViewabilitySessionTest {
    private static final String BUYER_RESOURCE_0 = "buyerResource0";
    private static final String BUYER_RESOURCE_1 = "buyerResource1";
    private static final String MOPUB_VIEWABILITY_TRACKER = "mopub_viewability_tracker";

    @Mock private WebView webView;
    @Mock private View view;
    @Mock private View obstruction0;
    @Mock private View obstruction1;

    private Activity activity;
    private AvidViewabilitySession subject;
    private Set<String> buyerResources = new HashSet<>();
    private Map<String, String> videoViewabilityTrackers = new TreeMap<>();
    private List<View> obstructions = new ArrayList<>();
    private AvidVideoPlaybackListener videoPlaybackListener;

    @Before
    public void setup() {
        subject = new AvidViewabilitySession();
        activity = Robolectric.buildActivity(Activity.class).create().get();
        videoPlaybackListener = ShadowAvidAdSessionManager.getMockVideoPlaybackListener();

        buyerResources.add(BUYER_RESOURCE_0);
        buyerResources.add(BUYER_RESOURCE_1);
        videoViewabilityTrackers.put("avid", MOPUB_VIEWABILITY_TRACKER);
        videoViewabilityTrackers.put("moat", "ignored");
        obstructions.add(obstruction0);
        obstructions.add(obstruction1);
    }

    @After
    public void tearDown() {
        AvidViewabilitySession.resetStaticState();
    }

    @Test
    public void getName_shouldReturnAvid() {
        assertThat(subject.getName()).isEqualTo("AVID");
    }

    @Test
    public void initialize_shouldReturnTrue() {
        Boolean result = subject.initialize(activity);

        assertThat(result).isTrue();
    }

    @Test
    public void initialize_whenVendorDisabled_shouldReturnNull() {
        AvidViewabilitySession.disable();

        Boolean result = subject.initialize(activity);

        assertThat(result).isNull();
    }

    @Test
    public void initialize_whenAvidMissing_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);

        Boolean result = subject.initialize(activity);

        assertThat(result).isNull();
    }

    @Test
    public void createDisplaySession_whenIsDeferredFalse_shouldStartAdSession_shouldUseNonDeferredContext_shouldReturnTrue() {
        assertThat(ShadowAvidAdSessionManager.wasStartDisplayAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayAvidAdSessionContext()).isNull();

        Boolean result = subject.createDisplaySession(activity, webView, false);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasStartDisplayAdSessionCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayContext()).isEqualTo(activity);
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayAvidAdSessionContext().getPartnerVersion()).isEqualTo(MoPub.SDK_VERSION);
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayAvidAdSessionContext().isDeferred()).isFalse();
    }

    @Test
    public void createDisplaySession_whenIsDeferredTrue_shouldStartAdSession_shouldUseDeferredContext_shouldReturnTrue() {
        Boolean result = subject.createDisplaySession(activity, webView, true);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasStartDisplayAdSessionCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayContext()).isEqualTo(activity);
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayAvidAdSessionContext().getPartnerVersion()).isEqualTo(MoPub.SDK_VERSION);
        assertThat(ShadowAvidAdSessionManager.getLatestDisplayAvidAdSessionContext().isDeferred()).isTrue();
    }

    @Test
    public void createDisplaySession_withActivityContext_shouldRegisterAdViewWithActivity_shouldReturnTrue() {
        assertThat(ShadowAvidAdSessionManager.wasRegisterDisplayAdViewCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayWebView()).isNull();
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayActivity()).isNull();

        Boolean result = subject.createDisplaySession(activity, webView, true);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasRegisterDisplayAdViewCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayWebView()).isEqualTo(webView);
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayActivity()).isEqualTo(activity);
    }

    @Test
    public void createDisplaySession_withApplicationContext_shouldRegisterAdViewWithNullActivity_shouldReturnTrue() {
        Boolean result = subject.createDisplaySession(activity.getApplicationContext(), webView, true);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasRegisterDisplayAdViewCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayWebView()).isEqualTo(webView);
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredDisplayActivity()).isNull();
    }

    @Test
    public void createDisplaySession_whenVendorDisabled_shouldNotStartSessionOrRegisterAdView_shouldReturnNull() {
        AvidViewabilitySession.disable();

        Boolean result = subject.createDisplaySession(activity, webView, false);

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasStartDisplayAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.wasRegisterDisplayAdViewCalled()).isFalse();
    }

    @Test
    public void createDisplaySession_whenAvidMissing_shouldNotStartSessionOrRegisterAdView_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);

        Boolean result = subject.createDisplaySession(activity, webView, false);

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasStartDisplayAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.wasRegisterDisplayAdViewCalled()).isFalse();
    }

    @Test
    public void startDeferredDisplaySession_shouldRegisterActivityAndRecordReadyEvent_shouldReturnTrue() {
        subject.createDisplaySession(activity, webView, true);

        Boolean result = subject.startDeferredDisplaySession(activity);

        assertThat(result).isTrue();
        assertThat(ShadowAvidManager.getLatestRegisteredActivity()).isEqualTo(activity);
        assertThat(ShadowAvidAdSessionManager.wasRecordDisplayReadyEventCalled()).isTrue();
    }

    @Test
    public void startDeferredDisplaySession_whenCreateDisplaySessionNeverCalled_shouldNotRegisterActivityAndNotRecordReadyEvent_shouldReturnFalse() {
        // create display session never called
        Boolean result = subject.startDeferredDisplaySession(activity);

        assertThat(result).isFalse();
        assertThat(ShadowAvidManager.getLatestRegisteredActivity()).isNull();
        assertThat(ShadowAvidAdSessionManager.wasRecordDisplayReadyEventCalled()).isFalse();
    }

    @Test
    public void startDeferredDisplaySession_whenVendorDisabled_shouldNotRegisterActivityAndNotRecordReadyEvent_shouldReturnNull() {
        AvidViewabilitySession.disable();
        subject.createDisplaySession(activity, webView, true);

        Boolean result = subject.startDeferredDisplaySession(activity);

        assertThat(result).isNull();
        assertThat(ShadowAvidManager.getLatestRegisteredActivity()).isNull();
        assertThat(ShadowAvidAdSessionManager.wasRecordDisplayReadyEventCalled()).isFalse();
    }

    @Test
    public void startDeferredDisplaySession_whenAvidMissing_shouldNotRegisterActivityAndNotRecordReadyEvent_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);
        subject.createDisplaySession(activity, webView, true);

        Boolean result = subject.startDeferredDisplaySession(activity);

        assertThat(result).isNull();
        assertThat(ShadowAvidManager.getLatestRegisteredActivity()).isNull();
        assertThat(ShadowAvidAdSessionManager.wasRecordDisplayReadyEventCalled()).isFalse();
    }

    @Test
    public void endDisplaySession_shouldCallEndSession_shouldReturnTrue() {
        subject.createDisplaySession(activity, webView, false);

        Boolean result = subject.endDisplaySession();

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasEndDisplaySessionCalled()).isTrue();
    }

    @Test
    public void endDisplaySession_whenCreateDisplaySessionNeverCalled_shouldNotCallEndSession_shouldReturnFalse() {
        // create display session never called

        Boolean result = subject.endDisplaySession();

        assertThat(result).isFalse();
        assertThat(ShadowAvidAdSessionManager.wasEndDisplaySessionCalled()).isFalse();
    }

    @Test
    public void endDisplaySession_whenVendorDisabled_shouldNotCallEndSession_shouldReturnNull() {
        AvidViewabilitySession.disable();
        subject.createDisplaySession(activity, webView, false);

        Boolean result = subject.endDisplaySession();

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasEndDisplaySessionCalled()).isFalse();
    }

    @Test
    public void endDisplaySession_whenAvidMissing_shouldNotCallEndSession_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);
        subject.createDisplaySession(activity, webView, false);

        Boolean result = subject.endDisplaySession();

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasEndDisplaySessionCalled()).isFalse();
    }

    @Test
    public void createVideoSession_shouldStartAdSession_shouldUseNonDeferredContext_shouldReturnTrue() {
        assertThat(ShadowAvidAdSessionManager.wasStartVideoAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoAvidAdSessionContext()).isNull();

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasStartVideoAdSessionCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoContext()).isEqualTo(activity);
        assertThat(ShadowAvidAdSessionManager.getLatestVideoAvidAdSessionContext().getPartnerVersion()).isEqualTo(MoPub.SDK_VERSION);
        assertThat(ShadowAvidAdSessionManager.getLatestVideoAvidAdSessionContext().isDeferred()).isFalse();
    }

    @Test
    public void createVideoSession_shouldRegisterAdView_shouldReturnTrue() {
        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasRegisterVideoAdViewCalled()).isTrue();
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredVideoView()).isEqualTo(view);
        assertThat(ShadowAvidAdSessionManager.getLatestRegisteredVideoActivity()).isEqualTo(activity);
    }

    @Test
    public void createVideoSession_shouldInjectJavascriptResourcesFromMoPubAndBuyerTags() {
        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources().size()).isEqualTo(3);
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).contains(
                MOPUB_VIEWABILITY_TRACKER, BUYER_RESOURCE_0, BUYER_RESOURCE_1
        );
    }

    @Test
    public void createVideoSession_withNullOrEmptyBuyerResources_shouldIgnoreInvalidJavascript_shouldReturnTrue() {
        buyerResources.add("");
        buyerResources.add("somethingValid");
        buyerResources.add(null);

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources().size()).isEqualTo(4);
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).contains(
                MOPUB_VIEWABILITY_TRACKER, BUYER_RESOURCE_0, BUYER_RESOURCE_1, "somethingValid"
        );
    }

    @Test
    public void createVideoSession_withMissingMoPubTag_shouldIgnoreInvalidJavascript_shouldReturnTrue() {
        videoViewabilityTrackers.remove("avid");

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources().size()).isEqualTo(2);
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).contains(
                BUYER_RESOURCE_0, BUYER_RESOURCE_1
        );
    }

    @Test
    public void createVideoSession_withEmptyMoPubTag_shouldIgnoreInvalidJavascript_shouldReturnTrue() {
        videoViewabilityTrackers.put("avid", "");

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources().size()).isEqualTo(2);
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).contains(
                BUYER_RESOURCE_0, BUYER_RESOURCE_1
        );
    }

    @Test
    public void createVideoSession_withVendorDisabled_shouldDoNothing_shouldReturnNull() {
        AvidViewabilitySession.disable();

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasStartVideoAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoAvidAdSessionContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).isEmpty();
    }

    @Test
    public void createVideoSession_withAvidMissing_shouldDoNothing_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);

        Boolean result = subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasStartVideoAdSessionCalled()).isFalse();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getLatestVideoAvidAdSessionContext()).isNull();
        assertThat(ShadowAvidAdSessionManager.getInjectedJavaScriptResources()).isEmpty();
    }

    @Test
    public void registerVideoObstructions_shouldCallThroughToRegisterFriendlyObstructions_shouldReturnTrue() {
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        for (final View obstruction : obstructions) {
            Boolean result = subject.registerVideoObstruction(obstruction);
            assertThat(result).isTrue();
        }

        assertThat(ShadowAvidAdSessionManager.getFriendlyObstructions().size()).isEqualTo(2);
        assertThat(ShadowAvidAdSessionManager.getFriendlyObstructions()).contains(
                obstruction0, obstruction1
        );
    }

    @Test
    public void registerVideoObstructions_whenCreateVideoSessionNeverCalled_shouldDoNothing_shouldReturnNull() {
        // create video session never called

        for (final View obstruction : obstructions) {
            Boolean result = subject.registerVideoObstruction(obstruction);
            assertThat(result).isFalse();
        }

        assertThat(ShadowAvidAdSessionManager.getFriendlyObstructions()).isEmpty();
    }

    @Test
    public void registerVideoObstructions_withVendorDisabled_shouldDoNothing_shouldReturnNull() {
        AvidViewabilitySession.disable();
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        for (final View obstruction : obstructions) {
            Boolean result = subject.registerVideoObstruction(obstruction);
            assertThat(result).isNull();
        }

        assertThat(ShadowAvidAdSessionManager.getFriendlyObstructions()).isEmpty();
    }

    @Test
    public void registerVideoObstructions_withAvidMissing_shouldDoNothing_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        for (final View obstruction : obstructions) {
            Boolean result = subject.registerVideoObstruction(obstruction);
            assertThat(result).isNull();
        }

        assertThat(ShadowAvidAdSessionManager.getFriendlyObstructions()).isEmpty();
    }

    @Test
    public void onVideoPrepared_shouldReturnTrue() {
        Boolean result = subject.onVideoPrepared(view, 0);

        assertThat(result).isTrue();
    }

    @Test
    public void onVideoPrepared_whenVendorDisabled_shouldReturnNull() {
        AvidViewabilitySession.disable();

        Boolean result = subject.onVideoPrepared(view, 0);

        assertThat(result).isNull();
    }

    @Test
    public void onVideoPrepared_whenAvidMissing_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);

        Boolean result = subject.onVideoPrepared(view, 0);

        assertThat(result).isNull();
    }

    @Test
    public void recordVideoEvent_withSupportedEvents_shouldNotifyListener_shouldReturnTrue() {
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);
        Boolean result;

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdLoadedEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_STARTED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdStartedEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_STOPPED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdStoppedEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_PAUSED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdPausedEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYING, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdPlayingEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_SKIPPED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdSkippedEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_IMPRESSED, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdImpressionEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_CLICK_THRU, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdClickThruEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_FIRST_QUARTILE, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdVideoFirstQuartileEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_MIDPOINT, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdVideoMidpointEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_THIRD_QUARTILE, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdVideoThirdQuartileEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_COMPLETE, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdCompleteEvent();

        result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, 0);
        assertThat(result).isTrue();
        verify(videoPlaybackListener).recordAdError("error");
    }

    @Test
    public void recordVideoEvent_whenCreateVideoSessionNeverCalled_shouldNotNotifyListener_shouldReturnFalse() {
        // create video session never called

        Boolean result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, 0);

        assertThat(result).isFalse();
        verifyZeroInteractions(videoPlaybackListener);
    }


    @Test
    public void recordVideoEvent_whenVendorDisabled_shouldNotNotifyListener_shouldReturnNull() {
        AvidViewabilitySession.disable();

        Boolean result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, 0);

        assertThat(result).isNull();
        verifyZeroInteractions(videoPlaybackListener);
    }

    @Test
    public void recordVideoEvent_whenAvidMissing_shouldNotNotifyListener_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);

        Boolean result = subject.recordVideoEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, 0);

        assertThat(result).isNull();
        verifyZeroInteractions(videoPlaybackListener);
    }

    @Test
    public void endVideoSession_shouldCallEndSession_shouldReturnTrue() {
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        Boolean result = subject.endVideoSession();

        assertThat(result).isTrue();
        assertThat(ShadowAvidAdSessionManager.wasEndVideoSessionCalled()).isTrue();
    }

    @Test
    public void endVideoSession_whenCreateVideoSessionNeverCalled_shouldNotCallEndSession_shouldReturnFalse() {
        // create video session never called

        Boolean result = subject.endVideoSession();

        assertThat(result).isFalse();
        assertThat(ShadowAvidAdSessionManager.wasEndVideoSessionCalled()).isFalse();
    }

    @Test
    public void endVideoSession_whenVendorDisabled_shouldNotCallEndSession_shouldReturnNull() {
        AvidViewabilitySession.disable();
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        Boolean result = subject.endVideoSession();

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasEndVideoSessionCalled()).isFalse();
    }

    @Test
    public void endVideoSession_whenAvidMissing_shouldNotCallEndSession_shouldReturnNull() {
        ShadowReflection.setNextClassNotFound(true);
        subject.createVideoSession(activity, view, buyerResources, videoViewabilityTrackers);

        Boolean result = subject.endVideoSession();

        assertThat(result).isNull();
        assertThat(ShadowAvidAdSessionManager.wasEndVideoSessionCalled()).isFalse();
    }
}
