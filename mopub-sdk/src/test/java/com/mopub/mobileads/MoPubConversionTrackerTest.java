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
import android.provider.Settings;

import com.mopub.common.GpsHelper;
import com.mopub.common.GpsHelperTest;
import com.mopub.common.util.Reflection.MethodBuilder;
import com.mopub.common.util.Utils;
import com.mopub.common.util.test.support.TestMethodBuilderFactory;
import com.mopub.mobileads.test.support.SdkTestRunner;

import org.apache.http.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(SdkTestRunner.class)
public class MoPubConversionTrackerTest {
    private MoPubConversionTracker subject;
    private Activity context;
    private FakeHttpLayer fakeHttpLayer;
    private MethodBuilder methodBuilder;
    private String expectedUdid;
    private boolean dnt = false;
    private static final String TEST_UDID = "20b013c721c";

    @Before
    public void setUp() throws Exception {
        subject = new MoPubConversionTracker();
        context = new Activity();
        fakeHttpLayer = Robolectric.getFakeHttpLayer();
        methodBuilder = TestMethodBuilderFactory.getSingletonMock();
        Settings.Secure.putString(application.getContentResolver(), Settings.Secure.ANDROID_ID, TEST_UDID);
        expectedUdid = "sha%3A" + Utils.sha1(TEST_UDID);
    }

    @After
    public void tearDown() throws Exception {
        reset(methodBuilder);
    }

    @Test
    public void reportAppOpen_onValidHttpResponse_isIdempotent() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isFalse();
    }

    @Test
    public void reportAppOpen_onInvalidStatusCode_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(404, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    @Test
    public void reportAppOpen_onEmptyResponse_shouldMakeSecondRequest() throws Exception {
        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();

        fakeHttpLayer.addPendingHttpResponse(200, "");
        subject.reportAppOpen(context);
        assertThat(requestWasMade()).isTrue();
    }

    @Test
    public void reportAppOpen_whenGooglePlayServicesIsLinkedAndAdInfoIsNotCached_shouldUseAdInfoParams() throws Exception {
        GpsHelper.setClassNamesForTesting();
        GpsHelperTest.verifyCleanSharedPreferences(context);
        GpsHelperTest.TestAdInfo adInfo = new GpsHelperTest.TestAdInfo();

        when(methodBuilder.setStatic(any(Class.class))).thenReturn(methodBuilder);
        when(methodBuilder.addParam(any(Class.class), any())).thenReturn(methodBuilder);
        when(methodBuilder.execute()).thenReturn(
                adInfo,
                adInfo.ADVERTISING_ID,
                adInfo.LIMIT_AD_TRACKING_ENABLED,
                GpsHelper.GOOGLE_PLAY_SUCCESS_CODE
        );

        expectedUdid = "ifa%3A" + adInfo.ADVERTISING_ID;
        dnt = true;

        fakeHttpLayer.addPendingHttpResponse(200, "doesn't matter what this is as long as it's not nothing");
        subject.reportAppOpen(context);
        Thread.sleep(500); // extra sleep since there are 2 async tasks
        assertThat(requestWasMade()).isTrue();
    }

    private boolean requestWasMade() throws Exception {
        StringBuilder stringBuilder = new StringBuilder("http://ads.mopub.com/m/open")
                .append("?v=6")
                .append("&id=").append("com.mopub.mobileads")
                .append("&udid=").append(expectedUdid);

        if (dnt) {
            stringBuilder.append("&dnt=1");
        }

        String expectedUrl = stringBuilder.append("&av=")
                .append("1.0")
                .toString();

        Thread.sleep(500);
        HttpRequestInfo lastSentHttpRequestInfo = fakeHttpLayer.getLastSentHttpRequestInfo();
        if (lastSentHttpRequestInfo == null) {
            return false;
        }
        HttpRequest request = lastSentHttpRequestInfo.getHttpRequest();
        fakeHttpLayer.clearRequestInfos();
        String actualUrl = request.getRequestLine().getUri();
        return actualUrl.equals(expectedUrl);
    }
}

