/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.test.support.TestAdViewControllerFactory;
import com.mopub.mobileads.test.support.TestCustomEventBannerAdapterFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import java.util.*;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static com.mopub.common.util.ResponseHeader.CUSTOM_EVENT_DATA;
import static com.mopub.common.util.ResponseHeader.CUSTOM_EVENT_HTML_DATA;
import static com.mopub.common.util.ResponseHeader.CUSTOM_EVENT_NAME;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MoPubViewTest {
    private MoPubView subject;
    private Map<String,String> paramsMap = new HashMap<String, String>();
    private CustomEventBannerAdapter customEventBannerAdapter;
    private AdViewController adViewController;
    private Context context;

    @Before
    public void setup() {
        context = new Activity();
        subject = new MoPubView(context);
        customEventBannerAdapter = TestCustomEventBannerAdapterFactory.getSingletonMock();
        reset(customEventBannerAdapter);
        adViewController = TestAdViewControllerFactory.getSingletonMock();
    }

    @Test
    public void screenStateBroadcastReceiver_withActionUserPresent_shouldUnpauseRefresh() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));

        verify(adViewController).unpauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withActionScreenOff_shouldPauseRefersh() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));

        verify(adViewController).pauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withNullIntent_shouldDoNothing() throws Exception {
        broadcastIntent(null);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).unpauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withRandomIntent_shouldDoNothing() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_BATTERY_LOW));

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).unpauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_whenAdInBackground_shouldDoNothing() throws Exception {
        subject.onWindowVisibilityChanged(View.INVISIBLE);
        reset(adViewController);

        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));
        verify(adViewController, never()).unpauseRefresh();

        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));
        verify(adViewController, never()).pauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_afterOnDestroy_shouldDoNothing() throws Exception {
        subject.destroy();

        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));
        verify(adViewController, never()).unpauseRefresh();

        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));
        verify(adViewController, never()).pauseRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_toVisible_shouldUnpauseRefresh() throws Exception {
        subject.onWindowVisibilityChanged(View.VISIBLE);

        verify(adViewController).unpauseRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_toInvisible_shouldPauseRefresh() throws Exception {
        subject.onWindowVisibilityChanged(View.INVISIBLE);

        verify(adViewController).pauseRefresh();
    }

    @Test
    public void setAutorefreshEnabled_withRefreshTrue_shouldForwardToAdViewController() throws Exception {
        subject.setAutorefreshEnabled(true);

        verify(adViewController).forceSetAutorefreshEnabled(true);
    }

    @Test
    public void setAutorefreshEnabled_withRefreshFalse_shouldForwardToAdViewController() throws Exception {
        subject.setAutorefreshEnabled(false);

        verify(adViewController).forceSetAutorefreshEnabled(false);
    }
    
    @Test
    public void nativeAdLoaded_shouldScheduleRefreshTimer() throws Exception {
        subject.nativeAdLoaded();

        verify(adViewController).scheduleRefreshTimerIfEnabled();
    }

    @Test
    public void loadCustomEvent_shouldInitializeCustomEventBannerAdapter() throws Exception {
        paramsMap.put(CUSTOM_EVENT_NAME.getKey(), "name");
        paramsMap.put(CUSTOM_EVENT_DATA.getKey(), "data");
        paramsMap.put(CUSTOM_EVENT_HTML_DATA.getKey(), "html");
        subject.loadCustomEvent(paramsMap);

        assertThat(TestCustomEventBannerAdapterFactory.getLatestMoPubView()).isEqualTo(subject);
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassName()).isEqualTo("name");
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassData()).isEqualTo("data");

        verify(customEventBannerAdapter).loadAd();
    }

    @Test
    public void loadCustomEvent_whenParamsMapIsNull_shouldCallLoadFailUrl() throws Exception {
        subject.loadCustomEvent(null);

        verify(adViewController).loadFailUrl(eq(ADAPTER_NOT_FOUND));
        verify(customEventBannerAdapter, never()).invalidate();
        verify(customEventBannerAdapter, never()).loadAd();
    }

    private void broadcastIntent(final Intent intent) {
        final List<ShadowApplication.Wrapper> wrappers = Robolectric.getShadowApplication().getRegisteredReceivers();

        for (final ShadowApplication.Wrapper wrapper : wrappers) {
            wrapper.broadcastReceiver.onReceive(context, intent);
        }
    }
}
