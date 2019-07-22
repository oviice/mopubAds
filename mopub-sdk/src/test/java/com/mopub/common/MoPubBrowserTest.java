// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.os.Build;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class MoPubBrowserTest {

    private MoPubBrowser subject;
    private WebView mockWebView;

    @Before
    public void setUp() {
        subject = Robolectric.buildActivity(MoPubBrowser.class).create().get();
        CookieSyncManager.createInstance(subject);

        mockWebView = mock(WebView.class);
    }

    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void moPubBrowser_shouldHaveSystemUiFlagsSet() throws Exception {
        final int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        subject.onResume();

        assertThat(subject.getSystemUiVisibility()).isEqualTo(flags);
    }

    @Config(sdk = Build.VERSION_CODES.KITKAT)
    @Test
    public void moPubBrowser_withApi19AndAbove_shouldHaveSystemUiFlagsSetWithImmersive() throws Exception {
        final int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        subject.onResume();

        assertThat(subject.getSystemUiVisibility()).isEqualTo(flags);
    }

    @Test
    public void onPause_withIsFinishingTrue_shouldStopLoading_shouldLoadBlankUrl_shouldPauseWebView() throws Exception {
        // We have to manually call #onPause here after #finish since the activity is not being managed by Android
        // Even if the activity was being managed by Android we would likely have to call onPause since the test would
        // complete before the UI thread had a chance to invoke the lifecycle events
        subject.setWebView(mockWebView);
        subject.finish();
        subject.onPause();

        verify(mockWebView).stopLoading();
        verify(mockWebView).loadUrl("");
        verify(mockWebView).onPause();
    }

    @Test
    public void onPause_withIsFinishingFalse_shouldPauseWebView() throws Exception {
        subject.setWebView(mockWebView);
        subject.onPause();

        verify(mockWebView, never()).stopLoading();
        verify(mockWebView, never()).loadUrl("");
        verify(mockWebView).onPause();
    }

    @Test
    public void onResume_shouldResumeWebView() throws Exception {
        subject.setWebView(mockWebView);
        subject.onResume();

        verify(mockWebView).onResume();
    }

    @Test
    public void onDestroy_shouldDestroyWebView() throws Exception {
        subject.setWebView(mockWebView);
        subject.onDestroy();

        verify(mockWebView).destroy();
    }
}
