// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common.util.test.support;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import com.integralads.avid.library.mopub.deferred.AvidDeferredAdSessionListener;
import com.integralads.avid.library.mopub.session.AvidAdSessionManager;
import com.integralads.avid.library.mopub.session.AvidDisplayAdSession;
import com.integralads.avid.library.mopub.session.AvidManagedVideoAdSession;
import com.integralads.avid.library.mopub.session.ExternalAvidAdSessionContext;
import com.integralads.avid.library.mopub.video.AvidVideoPlaybackListener;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Implements(value = AvidAdSessionManager.class)
public class ShadowAvidAdSessionManager {
    // AvidDisplayListener state
    private static boolean sRecordDisplayReadyEventCalled;

    // AvidAdSessionManager state
    private static boolean sStartDisplayAdSessionCalled;
    private static Context sLatestDisplayContext;
    private static ExternalAvidAdSessionContext sLatestDisplayAvidAdSessionContext;

    private static boolean sStartVideoAdSessionCalled;
    private static Context sLatestVideoContext;
    private static ExternalAvidAdSessionContext sLatestVideoAvidAdSessionContext;

    // AvidDisplayAdSession state
    private static boolean sRegisterDisplayAdViewCalled;
    private static WebView sLatestRegisteredDisplayWebView;
    private static Activity sLatestRegisteredDisplayActivity;
    private static boolean sEndDisplaySessionCalled;

    // AvidVideoAdSession state
    private static boolean sRegisterVideoAdViewCalled;
    private static View sLatestRegisteredVideoView;
    private static Activity sLatestRegisteredVideoActivity;
    private static List<String> sInjectedJavaScriptResources = new ArrayList<String>();
    private static List<View> sFriendlyObstructions = new ArrayList<View>();
    private static boolean sEndVideoSessionCalled;

    @Mock private static AvidDisplayAdSession sMockAvidDisplayAdSession;
    @Mock private static AvidManagedVideoAdSession sMockAvidVideoAdSession;
    @Mock private static AvidDeferredAdSessionListener sAvidDeferredAdSessionListener;
    @Mock private static AvidVideoPlaybackListener sAvidVideoPlaybackListener;

    static {
        initializeMocks();
    }

    public static void reset() {
        sStartDisplayAdSessionCalled = false;
        sLatestDisplayContext = null;
        sLatestDisplayAvidAdSessionContext = null;

        sRegisterDisplayAdViewCalled = false;
        sLatestRegisteredDisplayWebView = null;
        sLatestRegisteredDisplayActivity = null;
        sEndDisplaySessionCalled = false;

        sStartVideoAdSessionCalled = false;
        sLatestVideoContext = null;
        sLatestVideoAvidAdSessionContext = null;

        sRegisterVideoAdViewCalled = false;
        sLatestRegisteredVideoView = null;
        sLatestRegisteredVideoActivity = null;
        sInjectedJavaScriptResources.clear();
        sFriendlyObstructions.clear();
        sEndVideoSessionCalled = false;

        sRecordDisplayReadyEventCalled = false;

        initializeMocks();
    }

    // Display
    @Implementation
    public static AvidDisplayAdSession startAvidDisplayAdSession(Context context,
            ExternalAvidAdSessionContext avidAdSessionContext) {
        sStartDisplayAdSessionCalled = true;
        sLatestDisplayContext = context;
        sLatestDisplayAvidAdSessionContext = avidAdSessionContext;

        return sMockAvidDisplayAdSession;
    }

    public static boolean wasStartDisplayAdSessionCalled() {
        return sStartDisplayAdSessionCalled;
    }

    public static Context getLatestDisplayContext() {
        return sLatestDisplayContext;
    }

    public static ExternalAvidAdSessionContext getLatestDisplayAvidAdSessionContext() {
        return sLatestDisplayAvidAdSessionContext;
    }

    public static boolean wasRegisterDisplayAdViewCalled() {
        return sRegisterDisplayAdViewCalled;
    }

    public static WebView getLatestRegisteredDisplayWebView() {
        return sLatestRegisteredDisplayWebView;
    }

    public static Activity getLatestRegisteredDisplayActivity() {
        return sLatestRegisteredDisplayActivity;
    }

    public static boolean wasEndDisplaySessionCalled() {
        return sEndDisplaySessionCalled;
    }

    public static boolean wasRecordDisplayReadyEventCalled() {
        return sRecordDisplayReadyEventCalled;
    }

    // Video
    @Implementation
    public static AvidManagedVideoAdSession startAvidManagedVideoAdSession(Context context,
            ExternalAvidAdSessionContext avidAdSessionContext) {
        sStartVideoAdSessionCalled = true;
        sLatestVideoContext = context;
        sLatestVideoAvidAdSessionContext = avidAdSessionContext;
        return sMockAvidVideoAdSession;
    }

    public static boolean wasStartVideoAdSessionCalled() {
        return sStartVideoAdSessionCalled;
    }

    public static Context getLatestVideoContext() {
        return sLatestVideoContext;
    }

    public static ExternalAvidAdSessionContext getLatestVideoAvidAdSessionContext() {
        return sLatestVideoAvidAdSessionContext;
    }

    public static boolean wasRegisterVideoAdViewCalled() {
        return sRegisterVideoAdViewCalled;
    }

    public static View getLatestRegisteredVideoView() {
        return sLatestRegisteredVideoView;
    }

    public static Activity getLatestRegisteredVideoActivity() {
        return sLatestRegisteredVideoActivity;
    }

    public static List<String> getInjectedJavaScriptResources() {
        return sInjectedJavaScriptResources;
    }

    public static List<View> getFriendlyObstructions() {
        return sFriendlyObstructions;
    }

    public static AvidVideoPlaybackListener getMockVideoPlaybackListener() {
        return sAvidVideoPlaybackListener;
    }

    public static boolean wasEndVideoSessionCalled() {
        return sEndVideoSessionCalled;
    }

    private static void initializeMocks() {
        sMockAvidDisplayAdSession = mock(AvidDisplayAdSession.class);
        sMockAvidVideoAdSession = mock(AvidManagedVideoAdSession.class);
        sAvidDeferredAdSessionListener = mock(AvidDeferredAdSessionListener.class);
        sAvidVideoPlaybackListener = mock(AvidVideoPlaybackListener.class);

        // Listeners
        when(sMockAvidDisplayAdSession.getAvidDeferredAdSessionListener())
                .thenReturn(sAvidDeferredAdSessionListener);
        when(sMockAvidVideoAdSession.getAvidVideoPlaybackListener())
                .thenReturn(sAvidVideoPlaybackListener);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sRecordDisplayReadyEventCalled = true;
                return null;
            }
        }).when(sAvidDeferredAdSessionListener).recordReadyEvent();

        // Display
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sRegisterDisplayAdViewCalled = true;
                sLatestRegisteredDisplayWebView = (WebView) invocation.getArguments()[0];
                sLatestRegisteredDisplayActivity = (Activity) invocation.getArguments()[1];
                return null;
            }
        }).when(sMockAvidDisplayAdSession).registerAdView(any(WebView.class), any(Activity.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sEndDisplaySessionCalled = true;
                return null;
            }
        }).when(sMockAvidDisplayAdSession).endSession();

        // Video
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sRegisterVideoAdViewCalled = true;
                sLatestRegisteredVideoView = (View) invocation.getArguments()[0];
                sLatestRegisteredVideoActivity = (Activity) invocation.getArguments()[1];
                return null;
            }
        }).when(sMockAvidVideoAdSession).registerAdView(any(View.class), any(Activity.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sInjectedJavaScriptResources.add((String) invocation.getArguments()[0]);
                return null;
            }
        }).when(sMockAvidVideoAdSession).injectJavaScriptResource(any(String.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sFriendlyObstructions.add((View) invocation.getArguments()[0]);
                return null;
            }
        }).when(sMockAvidVideoAdSession).registerFriendlyObstruction(any(View.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sEndVideoSessionCalled = true;
                return null;
            }
        }).when(sMockAvidVideoAdSession).endSession();
    }
}
