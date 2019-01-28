// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common.util.test.support;

import android.app.Activity;
import android.webkit.WebView;

import com.moat.analytics.mobile.mpub.MoatFactory;
import com.moat.analytics.mobile.mpub.WebAdTracker;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@Implements(value = MoatFactory.class)
public class ShadowMoatFactory {
    // MoatFactory state
    private static boolean sCreateCalled;
    private static Activity sLatestActivity;
    private static boolean sCreateWebAdTrackerCalled;
    private static WebView sLatestWebView;

    // WebAdTracker state
    private static boolean sStartTrackingCalled;
    private static boolean sStopTrackingCalled;

    private static @Mock MoatFactory sMockMoatFactory;
    private static @Mock WebAdTracker sMockWebAdTracker;

    static {
        initializeMocks();
    }

    public static void reset() {
        sCreateCalled = false;
        sLatestActivity = null;
        sCreateWebAdTrackerCalled = false;
        sLatestWebView = null;

        sStartTrackingCalled = false;
        sStopTrackingCalled = false;

        initializeMocks();
    }

    @Implementation
    public static MoatFactory create() {
        sCreateCalled = true;

        return sMockMoatFactory;
    }

    public static boolean wasCreateCalled() {
        return sCreateCalled;
    }

    public static Activity getLatestActivity() {
        return sLatestActivity;
    }

    public static boolean wasCreateWebAdTrackerCalled() {
        return sCreateWebAdTrackerCalled;
    }

    public static WebView getLatestWebView() {
        return sLatestWebView;
    }

    public static boolean wasStartTrackingCalled() {
        return sStartTrackingCalled;
    }

    public static boolean wasStopTrackingCalled() {
        return sStopTrackingCalled;
    }

    private static void initializeMocks() {
        sMockMoatFactory = mock(MoatFactory.class);
        sMockWebAdTracker = mock(WebAdTracker.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sCreateWebAdTrackerCalled = true;
                sLatestWebView = (WebView) invocation.getArguments()[0];
                return sMockWebAdTracker;
            }
        }).when(sMockMoatFactory).createWebAdTracker(any(WebView.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sLatestActivity = (Activity) invocation.getArguments()[0];
                return sMockWebAdTracker;
            }
        }).when(sMockMoatFactory).createWebAdTracker(any(WebView.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sStartTrackingCalled = true;
                return null;
            }
        }).when(sMockWebAdTracker).startTracking();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sStopTrackingCalled = true;
                return null;
            }
        }).when(sMockWebAdTracker).stopTracking();
    }
}
