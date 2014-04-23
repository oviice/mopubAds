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

package com.mopub.mobileads.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.mopub.common.MoPubBrowser;
import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.robotium.R;
import com.robotium.solo.Solo;
import com.mopub.mobileads.robotium.RobotiumTestSupportActivity;

import static com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class RobotiumTestSupportActivityTest extends ActivityInstrumentationTestCase2<RobotiumTestSupportActivity>{
    private static final long NETWORK_SLEEP_TIME = 3000;
    private static final String BANNER_AD_UNIT_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY8fgRDA";
    private static final String INTERSTITIAL_AD_UNIT_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY6tERDA";

    private Solo solo;
    private EditText bannerEditText;
    private EditText interstitialEditText;
    private Button bannerLoadButton;
    private Button interstitialLoadButton;
    private Button interstitialShowButton;
    private MoPubView banner;

    public RobotiumTestSupportActivityTest() {
        super(RobotiumTestSupportActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());

        bannerEditText = (EditText) solo.getView(R.id.banner_adunit_id_field);
        interstitialEditText = (EditText) solo.getView(R.id.interstitial_adunit_id_field);

        bannerLoadButton = (Button) solo.getView(R.id.banner_load_button);
        interstitialLoadButton = (Button) solo.getView(R.id.interstitial_load_button);
        interstitialShowButton = (Button) solo.getView(R.id.interstitial_show_button);

        banner = (MoPubView) solo.getView(R.id.mopubview);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testMoPubBannerLoadAndClick() throws Exception {
        enterBannerAdUnitId();
        TestBannerAdListener listener = new TestBannerAdListener();
        banner.setBannerAdListener(listener);

        solo.clickOnView(bannerLoadButton);
        Thread.sleep(NETWORK_SLEEP_TIME);
        assertTrue("Banner was not loaded.", listener.bannerWasLoaded());

        solo.clickOnView(banner);
        Thread.sleep(NETWORK_SLEEP_TIME);
        assertTrue("Banner was not clicked.", listener.bannerWasClicked());

        solo.assertCurrentActivity("expected a MoPubBrowser", MoPubBrowser.class);
    }

    public void testMoPubInterstitialLoadShowAndClick() throws Exception {
        enterInterstitialAdUnitId();
        TestInterstitialAdListener listener = new TestInterstitialAdListener();
        ((RobotiumTestSupportActivity) solo.getCurrentActivity()).setInterstitialListener(listener);

        solo.clickOnView(interstitialLoadButton);
        Thread.sleep(NETWORK_SLEEP_TIME);
        assertTrue("Interstitial was not loaded.", listener.interstitialWasLoaded());

        solo.clickOnView(interstitialShowButton);
        Thread.sleep(NETWORK_SLEEP_TIME);
        assertTrue("Interstitial was not shown.", listener.interstitialWasShown());
        solo.assertCurrentActivity("expected MoPubActivity", MoPubActivity.class);

        solo.clickOnView(solo.getCurrentActivity().findViewById(android.R.id.content));
        Thread.sleep(NETWORK_SLEEP_TIME);
        solo.assertCurrentActivity("expected MoPubBrowser", MoPubBrowser.class);
    }

    public void testMoPubInterstitialLoadShowAndDismissWithCloseButton() throws Exception {
        enterInterstitialAdUnitId();
        TestInterstitialAdListener listener = new TestInterstitialAdListener();
        ((RobotiumTestSupportActivity) solo.getCurrentActivity()).setInterstitialListener(listener);

        solo.clickOnView(interstitialLoadButton);
        Thread.sleep(NETWORK_SLEEP_TIME);

        solo.clickOnView(interstitialShowButton);
        Thread.sleep(NETWORK_SLEEP_TIME);

        solo.clickOnImageButton(0);
        Thread.sleep(500);
        assertTrue("Interstitial was not dismissed.", listener.interstitialWasDismissed());
    }

    public void testMoPubInterstitialLoadShowAndDismissWithBackButton() throws Exception {
        enterInterstitialAdUnitId();
        TestInterstitialAdListener listener = new TestInterstitialAdListener();
        ((RobotiumTestSupportActivity) solo.getCurrentActivity()).setInterstitialListener(listener);

        solo.clickOnView(interstitialLoadButton);
        Thread.sleep(NETWORK_SLEEP_TIME);

        solo.clickOnView(interstitialShowButton);
        Thread.sleep(NETWORK_SLEEP_TIME);

        solo.goBack();
        assertTrue("Interstitial was not dismissed.", listener.interstitialWasDismissed());
    }

    private void enterBannerAdUnitId() {
        solo.enterText(bannerEditText, BANNER_AD_UNIT_ID);
    }

    private void enterInterstitialAdUnitId() {
        solo.enterText(interstitialEditText, INTERSTITIAL_AD_UNIT_ID);
    }

    private static class TestBannerAdListener implements MoPubView.BannerAdListener {
        private boolean bannerWasLoaded;
        private boolean bannerWasClicked;

        private boolean bannerWasLoaded() {
            return bannerWasLoaded;
        }

        private boolean bannerWasClicked() {
            return bannerWasClicked;
        }

        @Override
        public void onBannerLoaded(MoPubView banner) {
            bannerWasLoaded = true;
        }

        @Override
        public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        }

        @Override
        public void onBannerClicked(MoPubView banner) {
            bannerWasClicked = true;
        }

        @Override
        public void onBannerExpanded(MoPubView banner) {
        }

        @Override
        public void onBannerCollapsed(MoPubView banner) {
        }

        void reset() {
            bannerWasLoaded = false;
            bannerWasClicked = false;
        }
    }

    private static class TestInterstitialAdListener implements InterstitialAdListener {
        private boolean interstitialWasLoaded;
        private boolean interstitialWasShown;
        private boolean interstitialWasDismissed;
        private boolean interstitialWasClicked;

        private boolean interstitialWasLoaded() {
            return interstitialWasLoaded;
        }

        private boolean interstitialWasShown() {
            return interstitialWasShown;
        }

        private boolean isInterstitialWasClicked() {
            return interstitialWasClicked;
        }

        public boolean interstitialWasDismissed() {
            return interstitialWasDismissed;
        }

        @Override
        public void onInterstitialLoaded(MoPubInterstitial interstitial) {
            interstitialWasLoaded = true;
        }

        @Override
        public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        }

        @Override
        public void onInterstitialShown(MoPubInterstitial interstitial) {
            interstitialWasShown = true;
        }

        @Override
        public void onInterstitialClicked(MoPubInterstitial interstitial) {
            interstitialWasClicked = true;
        }

        @Override
        public void onInterstitialDismissed(MoPubInterstitial interstitial) {
            interstitialWasDismissed = true;
        }

        void reset() {
            interstitialWasLoaded = false;
            interstitialWasShown = false;
            interstitialWasClicked = false;
            interstitialWasDismissed = false;
        }
    }
}
