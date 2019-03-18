// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;

import com.mopub.common.AdReport;
import com.mopub.common.DataKeys;
import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import static com.mopub.common.DataKeys.BROADCAST_IDENTIFIER_KEY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class BaseInterstitialActivityTest {
    private BaseInterstitialActivity subject;
    private long broadcastIdentifier;

    @Mock
    AdReport mockAdReport;

    // Make a concrete version of the abstract class for testing purposes.
    private static class TestInterstitialActivity extends BaseInterstitialActivity {
        View view;

        @Override
        public View getAdView() {
            if (view == null) {
                view = new View(this);
            }
            return view;
        }
    }

    @Before
    public void setup() {
        broadcastIdentifier = 2222;
    }

    @Test
    public void onCreate_shouldCreateView() throws Exception {
        subject = Robolectric.buildActivity(TestInterstitialActivity.class).create().get();
        View adView = getContentView(subject).getChildAt(0);

        assertThat(adView).isNotNull();
    }

    @Test
    public void onDestroy_shouldCleanUpContentView() throws Exception {
        subject = Robolectric.buildActivity(TestInterstitialActivity.class).create().destroy().get();

        assertThat(getContentView(subject).getChildCount()).isEqualTo(0);
    }

    @Test
    public void getBroadcastIdentifier_shouldReturnBroadcastIdFromIntent() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        Intent intent = new Intent(context, TestInterstitialActivity.class);
        intent.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);

        subject = Robolectric.buildActivity(TestInterstitialActivity.class, intent)
                .create().get();
        assertThat(subject.getBroadcastIdentifier()).isEqualTo(2222L);
    }

    @Test
    public void getBroadcastIdentifier_withMissingBroadCastId_shouldReturnNull() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        Intent intent = new Intent(context, TestInterstitialActivity.class);
        // This intent is missing a broadcastidentifier extra.

        subject = Robolectric.buildActivity(TestInterstitialActivity.class, intent)
                .create().get();

        assertThat(subject.getBroadcastIdentifier()).isNull();
    }

    @Test
    public void getResponseString_withNullAdReport_shouldReturnNull() {
        Intent intent = new Intent()
                .putExtra(DataKeys.AD_REPORT_KEY, mockAdReport)
                .putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);

        subject = Robolectric.buildActivity(TestInterstitialActivity.class, intent)
                .create().get();

        assertThat(subject.getResponseString()).isNull();
    }

    @Test
    public void getResponseString_withNonNullAdReport_shouldReturnResponseString() {
        final String responseString = "this is a response string";
        when(mockAdReport.getResponseString()).thenReturn(responseString);

        Intent intent = new Intent()
                .putExtra(DataKeys.AD_REPORT_KEY, mockAdReport)
                .putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);

        subject = Robolectric.buildActivity(TestInterstitialActivity.class, intent)
                .create().get();

        assertThat(subject.getResponseString()).isEqualTo(responseString);
    }

    @Test
    public void staticGetResponseString_withNullAdReport_shouldReturnNull() {
        AdReport nullAdReport = null;

        assertThat(BaseInterstitialActivity.getResponseString(nullAdReport)).isNull();
    }

    @Test
    public void staticGetResponseString_withNonNullAdReport_shouldReturnResponseString() {
        final String responseString = "this is a response string";
        when(mockAdReport.getResponseString()).thenReturn(responseString);

        assertThat(BaseInterstitialActivity.getResponseString(mockAdReport))
                .isEqualTo(responseString);
    }

    protected FrameLayout getContentView(BaseInterstitialActivity subject) {
        return subject.getCloseableLayout();
    }
}
