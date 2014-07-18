package com.mopub.nativeads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mopub.common.DownloadResponse;
import com.mopub.common.util.ResponseHeader;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;

import java.util.List;
import java.util.Map;

import static com.mopub.nativeads.ImpressionTrackingManager.NativeResponseWrapper;
import static com.mopub.nativeads.MoPubNative.MoPubNativeListener.EMPTY_MOPUB_NATIVE_LISTENER;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class NativeResponseTest {

    private NativeResponse subject;
    private BaseForwardingNativeAd mNativeAd;
    private Activity context;
    private ViewGroup view;
    private MoPubNative.MoPubNativeListener moPubNativeListener;
    private NativeResponse subjectWMockBaseNativeAd;
    private NativeAdInterface mMockNativeAd;
    private boolean baseNativeAdRecordedImpression;
    private boolean baseNativeAdIsClicked;
    private DownloadResponse downloadResponse;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        mNativeAd = new BaseForwardingNativeAd() {
            @Override
            public void recordImpression() {
                baseNativeAdRecordedImpression = true;
            }

            @Override
            public void handleClick(final View view) {
                baseNativeAdIsClicked = true;
            }
        };
        mNativeAd.setTitle("title");
        mNativeAd.setText("text");
        mNativeAd.setMainImageUrl("mainImageUrl");
        mNativeAd.setIconImageUrl("iconImageUrl");
        mNativeAd.setClickDestinationUrl("clickDestinationUrl");
        mNativeAd.setCallToAction("callToAction");
        mNativeAd.addExtra("extra", "extraValue");
        mNativeAd.addExtra("extraImage", "extraImageUrl");
        mNativeAd.addImpressionTracker("impressionUrl");
        mNativeAd.setImpressionMinTimeViewed(500);

        view = new LinearLayout(context);

        final TestHttpResponseWithHeaders testHttpResponseWithHeaders = new TestHttpResponseWithHeaders(200, "");
        testHttpResponseWithHeaders.addHeader(ResponseHeader.IMPRESSION_URL.getKey(), "moPubImpressionTrackerUrl");
        testHttpResponseWithHeaders.addHeader(ResponseHeader.CLICKTHROUGH_URL.getKey(), "moPubClickTrackerUrl");
        downloadResponse = new DownloadResponse(testHttpResponseWithHeaders);

        moPubNativeListener = mock(MoPubNative.MoPubNativeListener.class);

        subject = new NativeResponse(context, downloadResponse, mNativeAd, moPubNativeListener);

        mMockNativeAd = mock(NativeAdInterface.class);
        subjectWMockBaseNativeAd = new NativeResponse(context, downloadResponse, mMockNativeAd, moPubNativeListener);
    }

    @After
    public void tearDown() throws Exception {
        ImpressionTrackingManager.clearTracking();
    }

    @Test
    public void getTitle_shouldReturnTitleFromBaseNativeAd() throws Exception {
        assertThat(subject.getTitle()).isEqualTo("title");
    }

    @Test
    public void getTitle_shouldReturnTextFromBaseNativeAd() throws Exception {
        assertThat(subject.getText()).isEqualTo("text");
    }

    @Test
    public void getMainImageUrl_shouldReturnMainImageUrlFromBaseNativeAd() throws Exception {
        assertThat(subject.getMainImageUrl()).isEqualTo("mainImageUrl");
    }

    @Test
    public void getIconImageUrl_shouldReturnIconImageUrlFromBaseNativeAd() throws Exception {
        assertThat(subject.getIconImageUrl()).isEqualTo("iconImageUrl");
    }

    @Test
    public void getClickDestinationUrl_shouldReturnClickDestinationUrlFromBaseNativeAd() throws Exception {
        assertThat(subject.getClickDestinationUrl()).isEqualTo("clickDestinationUrl");
    }

    @Test
    public void getCallToAction_shouldReturnCallToActionFromBaseNativeAd() throws Exception {
        assertThat(subject.getCallToAction()).isEqualTo("callToAction");
    }

    @Test
    public void getExtra_shouldReturnExtraFromBaseNativeAd() throws Exception {
        assertThat(subject.getExtra("extra")).isEqualTo("extraValue");
    }

    @Test
    public void getExtras_shouldReturnCopyOfExtrasMapFromBaseNativeAd() throws Exception {
        final Map<String, Object> extras = subject.getExtras();
        assertThat(extras.size()).isEqualTo(2);
        assertThat(extras.get("extra")).isEqualTo("extraValue");
        assertThat(extras.get("extraImage")).isEqualTo("extraImageUrl");
        assertThat(extras).isNotSameAs(mNativeAd.getExtras());
    }

    @Test
    public void getImpressionTrackers_shouldReturnImpressionTrackersFromMoPubAndFromBaseNativeAd() throws Exception {
        final List<String> impressionTrackers = subject.getImpressionTrackers();
        assertThat(impressionTrackers).containsOnly("moPubImpressionTrackerUrl", "impressionUrl");
    }

    @Test
    public void getImpressionMinTimeViewed_shouldReturnImpressionMinTimeViewedFromBaseNativeAd() throws Exception {
        assertThat(subject.getImpressionMinTimeViewed()).isEqualTo(500);
    }

    @Test
    public void getImpressionMinPercentageViewed_shouldReturnImpressionMinPercentageViewedFromBaseNativeAd() throws Exception {
        assertThat(subject.getImpressionMinPercentageViewed()).isEqualTo(50);
    }

    @Test
    public void getClickTracker_shouldReturnMoPubClickTracker() throws Exception {
        assertThat(subject.getClickTracker()).isEqualTo("moPubClickTrackerUrl");
    }
    
    @Test
    public void prepareImpression_shouldAddViewAndResponseToImpressionTrackingManagerAndCallPrepareImpressionOnBaseNativeAd() throws Exception {
        View view = ImpressionTrackingManagerTest.getViewMock(View.VISIBLE, 100, 100, 100, 100, true, true);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        subjectWMockBaseNativeAd.prepareImpression(view);
        final Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getPollingViews();
        assertThat(keptViews.size()).isEqualTo(1);
        assertThat(keptViews.get(view).mNativeResponse).isSameAs(subjectWMockBaseNativeAd);

        verify(mMockNativeAd).prepareImpression(view);
    }

    @Test
    public void prepareImpression_whenDestroyed_shouldReturnFast() throws Exception {
        subjectWMockBaseNativeAd.destroy();
        assertThat(subjectWMockBaseNativeAd.isDestroyed()).isTrue();
        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();

        subjectWMockBaseNativeAd.prepareImpression(view);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        verify(mMockNativeAd, never()).prepareImpression(view);
    }

    @Test
    public void prepareImpression_whenAlreadyImpressed_shouldReturnFast() throws Exception {
        subjectWMockBaseNativeAd.setRecordedImpression(true);
        assertThat(subjectWMockBaseNativeAd.getRecordedImpression()).isTrue();
        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();

        subjectWMockBaseNativeAd.prepareImpression(view);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        verify(mMockNativeAd, never()).prepareImpression(view);
    }

    @Test
    public void recordImpression_shouldRecordImpressionsAndCallIntoBaseNativeAdAndNotifyListenerIdempotently() throws Exception {
        Robolectric.getFakeHttpLayer().addPendingHttpResponse(200, "ok");
        Robolectric.getFakeHttpLayer().addPendingHttpResponse(200, "ok");
        assertThat(subject.getRecordedImpression()).isFalse();

        subject.recordImpression(view);

        assertThat(subject.getRecordedImpression()).isTrue();

        List<HttpRequestInfo> httpRequestInfos = Robolectric.getFakeHttpLayer().getSentHttpRequestInfos();
        assertThat(httpRequestInfos.size()).isEqualTo(2);
        assertThat(httpRequestInfos.get(0).getHttpRequest().getRequestLine().getUri()).isEqualTo("moPubImpressionTrackerUrl");
        assertThat(httpRequestInfos.get(1).getHttpRequest().getRequestLine().getUri()).isEqualTo("impressionUrl");

        assertThat(baseNativeAdRecordedImpression).isTrue();
        verify(moPubNativeListener).onNativeImpression(view);

        // reset state
        baseNativeAdRecordedImpression = false;
        Robolectric.getFakeHttpLayer().clearRequestInfos();
        reset(moPubNativeListener);

        // verify impression tracking doesn't fire again
        subject.recordImpression(view);
        assertThat(subject.getRecordedImpression()).isTrue();
        assertThat(Robolectric.getFakeHttpLayer().getSentHttpRequestInfos()).isEmpty();
        assertThat(baseNativeAdRecordedImpression).isFalse();
        verify(moPubNativeListener, never()).onNativeImpression(view);
    }

    @Test
    public void recordImpression_whenDestroyed_shouldReturnFast() throws Exception {
        subject.destroy();
        subject.recordImpression(view);
        assertThat(subject.getRecordedImpression()).isFalse();
        assertThat(Robolectric.getFakeHttpLayer().getSentHttpRequestInfos()).isEmpty();
        assertThat(baseNativeAdRecordedImpression).isFalse();
        verify(moPubNativeListener, never()).onNativeImpression(view);
    }

    @Test
    public void handleClick_withNoBaseNativeAdClickDestinationUrl_shouldRecordClickAndCallIntoBaseNativeAdAndNotifyListener() throws Exception {
        Robolectric.getFakeHttpLayer().addPendingHttpResponse(200, "ok");
        assertThat(subject.isClicked()).isFalse();

        subject.handleClick(view);

        assertThat(subject.isClicked()).isTrue();

        List<HttpRequestInfo> httpRequestInfos = Robolectric.getFakeHttpLayer().getSentHttpRequestInfos();
        assertThat(httpRequestInfos.size()).isEqualTo(1);
        assertThat(httpRequestInfos.get(0).getHttpRequest().getRequestLine().getUri()).isEqualTo("moPubClickTrackerUrl");

        assertThat(baseNativeAdIsClicked).isTrue();
        verify(moPubNativeListener).onNativeClick(view);

        // reset state
        baseNativeAdIsClicked = false;
        Robolectric.getFakeHttpLayer().clearRequestInfos();
        reset(moPubNativeListener);

        // second time, tracking does not fire
        subject.handleClick(view);
        assertThat(subject.isClicked()).isTrue();
        assertThat(Robolectric.getFakeHttpLayer().getSentHttpRequestInfos()).isEmpty();
        assertThat(baseNativeAdRecordedImpression).isFalse();
        verify(moPubNativeListener).onNativeClick(view);
    }

    @Ignore("pending")
    @Test
    public void handleClick_withBaseNativeAdClickDestinationUrl_shouldRecordClickAndCallIntoBaseNativeAdAndOpenClickDestinationAndNotifyListener() throws Exception {
        // Really difficult to test url resolution since it doesn't use the apache http client
    }

    @Test
    public void handleClick_whenDestroyed_shouldReturnFast() throws Exception {
        subject.destroy();
        subject.handleClick(view);
        assertThat(subject.isClicked()).isFalse();
        assertThat(Robolectric.getFakeHttpLayer().getSentHttpRequestInfos()).isEmpty();
        assertThat(baseNativeAdIsClicked).isFalse();
        verify(moPubNativeListener, never()).onNativeClick(view);
    }

    @Test
    public void destroy_shouldCallIntoBaseNativeAd() throws Exception {
        subjectWMockBaseNativeAd.destroy();
        assertThat(subjectWMockBaseNativeAd.isDestroyed()).isTrue();
        verify(mMockNativeAd).destroy();

        reset(mMockNativeAd);

        subjectWMockBaseNativeAd.destroy();
        verify(mMockNativeAd, never()).destroy();
    }

    @Test
    public void destroy_shouldSetMoPubNativeListenerToEmptyMoPubNativeListener() throws Exception {
        assertThat(subjectWMockBaseNativeAd.getMoPubNativeListener()).isSameAs(moPubNativeListener);

        subjectWMockBaseNativeAd.destroy();

        assertThat(subjectWMockBaseNativeAd.getMoPubNativeListener()).isSameAs(EMPTY_MOPUB_NATIVE_LISTENER);
    }

    @Ignore("pending")
    @Test
    public void loadExtrasImage_shouldAsyncLoadImages() throws Exception {
        // no easy way to test this since nothing can be mocked
        // also not a critical test since it directly calls another service
    }
}
