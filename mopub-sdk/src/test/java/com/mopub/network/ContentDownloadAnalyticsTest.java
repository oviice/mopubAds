package com.mopub.network;

import android.app.Activity;

import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.mobileads.BuildConfig;
import com.mopub.mobileads.MoPubErrorCode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
@Config(constants = BuildConfig.class)
public class ContentDownloadAnalyticsTest {
    private static final String AFTER_LOAD_URL = "https://ads.mopub.com/m/load?load_duration_ms=%%LOAD_DURATION_MS%%&load_result=%%LOAD_RESULT%%";

    @Mock
    private MoPubRequestQueue mockRequestQueue;
    @Mock
    private AdResponse mockAdResponse;
    @Mock
    private AdResponse mockInvalidAdResponse;

    private Activity activity;
    private ContentDownloadAnalytics subject;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
        when(mockAdResponse.getBeforeLoadUrl()).thenReturn("before_load_url");
        when(mockAdResponse.getAfterLoadUrl()).thenReturn(AFTER_LOAD_URL);
        Networking.setRequestQueueForTesting(mockRequestQueue);
    }

    @Test
    public void reportBeforeLoad_withValidAdResponse_shouldAddRequestToNetworkQueue(){
        subject = new ContentDownloadAnalytics(mockAdResponse);

        subject.reportBeforeLoad(activity);

        assertThat(subject.mBeforeLoadTime).isNotZero();
        ArgumentCaptor<MoPubRequest> reqeustCaptor = ArgumentCaptor.forClass(MoPubRequest.class);
        verify(mockRequestQueue).add(reqeustCaptor.capture());
        MoPubRequest moPubRequest = reqeustCaptor.getValue();
        assertThat(moPubRequest.getOriginalUrl()).isEqualTo("before_load_url");
    }

    @Test
    public void reportBeforeLoad_withEmptyUrl_shouldNotSendRequest(){
        subject = new ContentDownloadAnalytics(mockInvalidAdResponse);

        subject.reportBeforeLoad(activity);

        verify(mockRequestQueue, never()).add(any(MoPubRequest.class));
        assertThat(subject.mBeforeLoadTime).isNull();
    }

    @Test
    public void reportAfterLoad_withValidAdResponse_withNoError_shouldSendNoErrorRequest(){
        subject = new ContentDownloadAnalytics(mockAdResponse);
        subject.reportBeforeLoad(activity);
        reset(mockRequestQueue);

        subject.reportAfterLoad(activity, MoPubErrorCode.AD_SUCCESS);

        ArgumentCaptor<MoPubRequest> reqeustCaptor = ArgumentCaptor.forClass(MoPubRequest.class);
        verify(mockRequestQueue).add(reqeustCaptor.capture());
        MoPubRequest moPubRequest = reqeustCaptor.getValue();
        assertThat(moPubRequest.getOriginalUrl().indexOf("ad_loaded")).isNotNegative();
    }

    @Test
    public void reportAfterLoad_withValidAdResponse_withTimeoutError_shouldSendTimeoutErrorRequest(){
        subject = new ContentDownloadAnalytics(mockAdResponse);
        subject.reportBeforeLoad(activity);
        reset(mockRequestQueue);

        subject.reportAfterLoad(activity, MoPubErrorCode.NETWORK_TIMEOUT);

        ArgumentCaptor<MoPubRequest> reqeustCaptor = ArgumentCaptor.forClass(MoPubRequest.class);
        verify(mockRequestQueue).add(reqeustCaptor.capture());
        MoPubRequest moPubRequest = reqeustCaptor.getValue();
        assertThat(moPubRequest.getOriginalUrl().indexOf("ad_loaded")).isNegative();
        assertThat(moPubRequest.getOriginalUrl().indexOf("timeout")).isNotNegative();
    }

    @Test
    public void reportAfterLoad_withEmptyUrl_shouldNotSendRequest(){
        when(mockInvalidAdResponse.getBeforeLoadUrl()).thenReturn("before_load_url");
        subject = new ContentDownloadAnalytics(mockInvalidAdResponse);
        subject.reportBeforeLoad(activity);
        reset(mockRequestQueue);

        subject.reportAfterLoad(activity, MoPubErrorCode.NETWORK_TIMEOUT);

        verify(mockRequestQueue, never()).add(any(MoPubRequest.class));
    }

    @Test
    public void reportAfterLoad_withBrokenUrl_shouldNotSendRequest(){
        when(mockInvalidAdResponse.getBeforeLoadUrl()).thenReturn("before_load_url");
        when(mockInvalidAdResponse.getAfterLoadUrl()).thenReturn("broken_url");
        subject = new ContentDownloadAnalytics(mockInvalidAdResponse);
        subject.reportBeforeLoad(activity);
        reset(mockRequestQueue);

        subject.reportAfterLoad(activity, MoPubErrorCode.NETWORK_TIMEOUT);

        verify(mockRequestQueue, never()).add(any(MoPubRequest.class));
    }

    @Test
    public void reportAfterLoad_withoutCallingReportBeforeLoad_shouldNotSendRequest(){
        subject = new ContentDownloadAnalytics(mockAdResponse);

        subject.reportAfterLoad(activity, MoPubErrorCode.NETWORK_TIMEOUT);

        verify(mockRequestQueue, never()).add(any(MoPubRequest.class));
    }
}
