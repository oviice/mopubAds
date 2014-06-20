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
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.mopub.mobileads.factories.CustomEventInterstitialAdapterFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;

import org.fest.util.Lists;
import org.fest.util.Sets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLocalBroadcastManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static com.mopub.mobileads.AdFetcher.HTML_RESPONSE_BODY_KEY;
import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_CLICK;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_DISMISS;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_FAIL;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.ACTION_INTERSTITIAL_SHOW;
import static com.mopub.mobileads.EventForwardingBroadcastReceiver.getHtmlInterstitialIntentFilter;
import static com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(SdkTestRunner.class)
public class EventForwardingBroadcastReceiverTest {

    private CustomEventInterstitialListener customEventInterstitialListener;
    private EventForwardingBroadcastReceiver subject;
    private Activity context;
    private int broadcastIdentifier;

    @Before
    public void setUp() throws Exception {
        customEventInterstitialListener = mock(CustomEventInterstitialListener.class);
        broadcastIdentifier = 27027027;
        subject = new EventForwardingBroadcastReceiver(customEventInterstitialListener, broadcastIdentifier);
        context = new Activity();
    }

    @Ignore("pending")
    @Test
    public void twoDifferentInterstitials_shouldNotHearEachOthersBroadcasts() throws Exception {
        final MoPubInterstitial interstitialA = new MoPubInterstitial(context, "adunitid");
        final InterstitialAdListener listenerA = mock(InterstitialAdListener.class);
        interstitialA.setInterstitialAdListener(listenerA);

        final MoPubInterstitial interstitialB = new MoPubInterstitial(context, "adunitid");
        final InterstitialAdListener listenerB = mock(InterstitialAdListener.class);
        interstitialB.setInterstitialAdListener(listenerB);

        final CustomEventInterstitialAdapter customEventInterstitialAdapter =
                CustomEventInterstitialAdapterFactory.create(
                        interstitialA,
                        "com.mopub.mobileads.HtmlInterstitial",
                        "{" + HTML_RESPONSE_BODY_KEY + ":response}");

        customEventInterstitialAdapter.loadInterstitial();
        verify(listenerA).onInterstitialLoaded(interstitialA);
        verify(listenerB, never()).onInterstitialLoaded(any(MoPubInterstitial.class));

        interstitialA.onCustomEventInterstitialShown();
        verify(listenerA).onInterstitialLoaded(interstitialA);
        verify(listenerB, never()).onInterstitialShown(any(MoPubInterstitial.class));

        interstitialA.onCustomEventInterstitialClicked();
        verify(listenerA).onInterstitialClicked(interstitialA);
        verify(listenerB, never()).onInterstitialClicked(any(MoPubInterstitial.class));

        interstitialA.onCustomEventInterstitialDismissed();
        verify(listenerA).onInterstitialDismissed(interstitialA);
        verify(listenerB, never()).onInterstitialDismissed(any(MoPubInterstitial.class));
    }

    @Test
    public void constructor_shouldSetIntentFilter() throws Exception {
        Set<String> expectedActions = Sets.newLinkedHashSet(
                ACTION_INTERSTITIAL_FAIL,
                ACTION_INTERSTITIAL_SHOW,
                ACTION_INTERSTITIAL_DISMISS,
                ACTION_INTERSTITIAL_CLICK
        );

        final IntentFilter intentFilter = EventForwardingBroadcastReceiver.getHtmlInterstitialIntentFilter();
        final Iterator<String> actionIterator = intentFilter.actionsIterator();

        assertThat(intentFilter.countActions()).isEqualTo(4);
        while (actionIterator.hasNext()) {
            assertThat(expectedActions.contains(actionIterator.next()));
        }
    }

    @Test
    public void onReceive_whenActionInterstitialFail_shouldNotifyListener() throws Exception {
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_CLICK, broadcastIdentifier);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialClicked();
    }

    @Test
    public void onReceive_whenActionInterstitialShow_shouldNotifyListener() throws Exception {
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, broadcastIdentifier);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialShown();
    }


    @Test
    public void onReceive_whenActionInterstitialDismiss_shouldNotifyListener() throws Exception {
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_DISMISS, broadcastIdentifier);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialDismissed();
    }

    @Test
    public void onReceive_whenActionInterstitialClick_shouldNotifyListener() throws Exception {
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_CLICK, broadcastIdentifier);

        subject.onReceive(context, intent);

        verify(customEventInterstitialListener).onInterstitialClicked();
    }

    @Test
    public void onReceive_withIncorrectBroadcastIdentifier_shouldDoNothing() throws Exception {
        long incorrectBroadcastIdentifier = broadcastIdentifier + 1;

        Intent fail = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_FAIL, incorrectBroadcastIdentifier);
        Intent show = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, incorrectBroadcastIdentifier);
        Intent click = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_CLICK, incorrectBroadcastIdentifier);
        Intent dismiss = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_DISMISS, incorrectBroadcastIdentifier);

        subject.onReceive(context, fail);
        subject.onReceive(context, show);
        subject.onReceive(context, click);
        subject.onReceive(context, dismiss);

        verifyNoMoreInteractions(customEventInterstitialListener);
    }

    @Test
    public void onReceiver_whenCustomEventInterstitialListenerIsNull_shouldNotBlowUp() throws Exception {
        Intent intent = new Intent(ACTION_INTERSTITIAL_SHOW);

        subject = new EventForwardingBroadcastReceiver(null, broadcastIdentifier);
        subject.onReceive(context, intent);

        // pass
    }

    @Test
    public void register_shouldEnableReceivingBroadcasts() throws Exception {
        subject.register(context);
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, broadcastIdentifier);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener).onInterstitialShown();
    }

    @Test
    public void unregister_shouldDisableReceivingBroadcasts() throws Exception {
        subject.register(context);

        subject.unregister();
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, broadcastIdentifier);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        verify(customEventInterstitialListener, never()).onInterstitialShown();
    }

    @Test
    public void unregister_whenNotRegistered_shouldNotBlowUp() throws Exception {
        subject.unregister();

        // pass
    }

    @Test
    public void unregister_shouldNotLeakTheContext() throws Exception {
        subject.register(context);
        subject.unregister();

        LocalBroadcastManager.getInstance(context).registerReceiver(subject, getHtmlInterstitialIntentFilter());
        subject.unregister();

        // Unregister shouldn't know the context any more and so should not have worked
        Intent intent = getIntentForActionAndIdentifier(ACTION_INTERSTITIAL_SHOW, broadcastIdentifier);
        ShadowLocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        verify(customEventInterstitialListener).onInterstitialShown();
    }

    static Intent getIntentForActionAndIdentifier(final String action, final long broadcastIdentifier) {
        final Intent intent = new Intent(action);
        intent.putExtra("broadcastIdentifier", broadcastIdentifier);
        return intent;
    }
}
