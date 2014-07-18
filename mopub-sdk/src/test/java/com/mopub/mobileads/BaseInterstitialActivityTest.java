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
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import org.fest.assertions.api.ANDROID;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import static com.mopub.common.util.Drawables.INTERSTITIAL_CLOSE_BUTTON_NORMAL;
import static com.mopub.common.util.Drawables.INTERSTITIAL_CLOSE_BUTTON_PRESSED;
import static com.mopub.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.getHtmlInterstitialIntentFilter;
import static com.mopub.mobileads.EventForwardingBroadcastReceiverTest.getIntentForActionAndIdentifier;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@Ignore
public class BaseInterstitialActivityTest {
    public static final String EXPECTED_SOURCE = "expected source";

    protected BaseInterstitialActivity subject;
    protected BroadcastReceiver broadcastReceiver;
    protected AdConfiguration adConfiguration;
    protected long testBroadcastIdentifier;

    public void setup() {
        broadcastReceiver = mock(BroadcastReceiver.class);
        testBroadcastIdentifier = 2222;
    }

    @Test
    public void onCreate_shouldBroadcastInterstitialShow() throws Exception {
        Intent expectedIntent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, testBroadcastIdentifier);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, getHtmlInterstitialIntentFilter());

        subject.onCreate(null);

        verify(broadcastReceiver).onReceive(any(Context.class), eq(expectedIntent));
    }

    @Test
    public void onCreate_shouldCreateView() throws Exception {
        subject.onCreate(null);

        View adView = getContentView(subject).getChildAt(0);

        assertThat(adView).isNotNull();
    }

    @Test
    public void onCreate_shouldShowInterstitialCloseButton() throws Exception {
        subject.onCreate(null);

        ImageButton closeButton = getCloseButton();

        Robolectric.clickOn(closeButton);

        ANDROID.assertThat(subject).isFinishing();
    }

    @Test
    public void onCreate_shouldMakeCloseButtonVisible() throws Exception {
        subject.onCreate(null);

        ImageButton closeButton = getCloseButton();

        ANDROID.assertThat(closeButton).isVisible();
        StateListDrawable states = (StateListDrawable) closeButton.getDrawable();

        int[] unpressedState = new int[] {-android.R.attr.state_pressed};
        assertThat(shadowOf(states).getDrawableForState(unpressedState))
                .isEqualTo(INTERSTITIAL_CLOSE_BUTTON_NORMAL.decodeImage(new Activity()));
        int[] pressedState = new int[] {android.R.attr.state_pressed};
        assertThat(shadowOf(states).getDrawableForState(pressedState))
                .isEqualTo(INTERSTITIAL_CLOSE_BUTTON_PRESSED.decodeImage(new Activity()));
    }

    @Test
    public void canShowAndHideTheCloseButton() throws Exception {
        subject.onCreate(null);
        ANDROID.assertThat(getCloseButton()).isVisible();

        subject.hideInterstitialCloseButton();
        ANDROID.assertThat(getCloseButton()).isInvisible();

        subject.showInterstitialCloseButton();
        ANDROID.assertThat(getCloseButton()).isVisible();
    }

    @Test
    public void onDestroy_shouldCleanUpContentView() throws Exception {
        subject.onCreate(null);
        subject.onDestroy();

        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void onDestroy_shouldBroadcastInterstitialDismiss() throws Exception {
        Intent expectedIntent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_DISMISS, testBroadcastIdentifier);
        ShadowLocalBroadcastManager.getInstance(subject).registerReceiver(broadcastReceiver, getHtmlInterstitialIntentFilter());

        subject.onCreate(null);
        subject.onDestroy();

        verify(broadcastReceiver).onReceive(any(Context.class), eq(expectedIntent));
    }

    @Test
    public void getAdConfiguration_shouldReturnAdConfigurationFromIntent() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(AD_CONFIGURATION_KEY, adConfiguration);

        subject.onCreate(null);
        subject.setIntent(intent);

        assertThat(subject.getAdConfiguration()).isNotNull();
    }

    @Test
    public void getAdConfiguration_withMissingOrWrongAdConfiguration_shouldReturnNull() throws Exception {
        Intent intent = new Intent();
        // This intent is missing an AdConfiguration extra.

        subject.onCreate(null);
        subject.setIntent(intent);

        assertThat(subject.getAdConfiguration()).isNull();
    }

    protected ImageButton getCloseButton() {
        return (ImageButton) getContentView(subject).getChildAt(1);
    }

    protected RelativeLayout getContentView(BaseInterstitialActivity subject) {
        return (RelativeLayout) ((ViewGroup) subject.findViewById(android.R.id.content)).getChildAt(0);
    }

    protected void resetMockedView(View view) {
        reset(view);
        stub(view.getLayoutParams()).toReturn(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
    }
}
