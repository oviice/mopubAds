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

import com.mopub.common.util.DeviceUtils;
import com.mopub.common.util.Utils;
import com.mopub.mobileads.test.support.SdkTestRunner;
import com.mopub.mobileads.util.vast.VastVideoConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.mopub.mobileads.BaseVideoPlayerActivity.VIDEO_URL;
import static com.mopub.mobileads.BaseVideoPlayerActivity.startMraid;
import static com.mopub.mobileads.BaseVideoPlayerActivity.startVast;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(SdkTestRunner.class)
public class BaseVideoPlayerActivitiyTest {
    private static final String MRAID_VIDEO_URL = "http://mraidVideo";

    private AdConfiguration adConfiguration;
    private long testBroadcastIdentifier;
    private VastVideoConfiguration vastVideoConfiguration;

    @Before
    public void setup() throws Exception {
        adConfiguration = mock(AdConfiguration.class, withSettings().serializable());
        vastVideoConfiguration = mock(VastVideoConfiguration.class, withSettings().serializable());

        testBroadcastIdentifier = 1234;
        when(adConfiguration.getBroadcastIdentifier()).thenReturn(testBroadcastIdentifier);
    }

    @Test
    public void startMraid_shouldStartMraidVideoPlayerActivity() throws Exception {
        startMraid(new Activity(), MRAID_VIDEO_URL, adConfiguration);
        assertMraidVideoPlayerActivityStarted(MraidVideoPlayerActivity.class, MRAID_VIDEO_URL, adConfiguration);
    }

    @Test
    public void startVast_shouldStartMraidVideoPlayerActivity() throws Exception {
        startVast(new Activity(), vastVideoConfiguration, adConfiguration);
        assertVastVideoPlayerActivityStarted(MraidVideoPlayerActivity.class, vastVideoConfiguration, adConfiguration);
    }

    static void assertVastVideoPlayerActivityStarted(final Class clazz,
            final VastVideoConfiguration vastVideoConfiguration,
            final AdConfiguration adConfiguration) {
        final Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertIntentAndAdConfigurationAreCorrect(intent, clazz, adConfiguration);

        final VastVideoConfiguration expectedVastVideoConfiguration =
                (VastVideoConfiguration) intent.getSerializableExtra(VastVideoViewController.VAST_VIDEO_CONFIGURATION);
        assertThat(expectedVastVideoConfiguration).isEqualsToByComparingFields(vastVideoConfiguration);
    }

    static void assertMraidVideoPlayerActivityStarted(final Class clazz,
            final String url,
            final AdConfiguration adConfiguration) {
        final Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
        assertIntentAndAdConfigurationAreCorrect(intent, clazz, adConfiguration);

        assertThat(intent.getStringExtra(VIDEO_URL)).isEqualTo(url);
    }

    static void assertIntentAndAdConfigurationAreCorrect(final Intent intent,
            final Class clazz,
            final AdConfiguration adConfiguration) {
        assertThat(intent.getComponent().getClassName()).isEqualTo(clazz.getCanonicalName());
        assertThat(Utils.bitMaskContainsFlag(intent.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK)).isTrue();

        final AdConfiguration expectedAdConfiguration = (AdConfiguration) intent.getSerializableExtra(AD_CONFIGURATION_KEY);
        assertThat(expectedAdConfiguration).isEqualsToByComparingFields(adConfiguration);
    }
}
