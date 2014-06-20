package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import com.mopub.common.DownloadResponse;
import com.mopub.common.util.ResponseHeader;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.util.Map;

import static com.mopub.nativeads.ImpressionTrackingManager.NativeResponseWrapper;
import static com.mopub.nativeads.ImpressionTrackingManager.VisibilityCheck;
import static com.mopub.nativeads.MoPubNative.MoPubNativeListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class ImpressionTrackingManagerTest {
    private static final String IMPRESSION_TRACKER = "url1";

    private View view;
    private NativeResponse nativeResponse;
    private NativeResponseWrapper nativeResponseWrapper;
    private Context context;
    private MoPubNativeListener mopubNativeListener;

    @Before
    public void setUp() throws Exception {
        ImpressionTrackingManager.purgeViews();

        context = new Activity();
        mopubNativeListener = mock(MoPubNativeListener.class);
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100);

        final BaseForwardingNativeAd nativeAd = new BaseForwardingNativeAd() {};
        final TestHttpResponseWithHeaders testHttpResponseWithHeaders = new TestHttpResponseWithHeaders(200, "");
        testHttpResponseWithHeaders.addHeader(ResponseHeader.IMPRESSION_URL.getKey(), "url1");
        final DownloadResponse downloadResponse = new DownloadResponse(testHttpResponseWithHeaders);

        nativeResponse = new NativeResponse(context, downloadResponse, nativeAd, mopubNativeListener);
        nativeResponseWrapper = new NativeResponseWrapper(nativeResponse);

        Robolectric.addPendingHttpResponse(new TestHttpResponse(200, ""));

        // We need this to ensure that our SystemClock starts
        ShadowSystemClock.uptimeMillis();
    }

    @After
    public void tearDown() throws Exception {
        ImpressionTrackingManager.purgeViews();
    }

    @Ignore("pending")
    @Test
    public void start_shouldScheduleVisibilityCheck() throws Exception {
        Robolectric.getBackgroundScheduler().pause();
        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(0);
        ImpressionTrackingManager.start();
        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Ignore("pending")
    @Test
    public void start_onSubsequentInvocations_shouldDoNothing() throws Exception {
    }

    @Ignore("pending")
    @Test
    public void stop_shouldCancelVisibilityChecks() throws Exception {
    }

    @Ignore("pending")
    @Test
    public void stop_beforeStartIsCalled_doesNothing() throws Exception {
    }

    @Test
    public void addView_shouldAddViewToHashMap() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).hasSize(1);
        assertThat(keptViews.get(view).mNativeResponse).isEqualTo(nativeResponse);
    }

    @Test
    public void addView_whenViewIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(null, nativeResponse);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).isEmpty();
    }

    @Test
    public void addView_whenNativeResponseIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(view, null);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).isEmpty();
    }

    @Test
    public void removeView_shouldRemoveViewFromKeptViews() throws Exception {
        View view1 = mock(View.class);
        View view2 = mock(View.class);

        ImpressionTrackingManager.addView(view1, nativeResponse);
        ImpressionTrackingManager.addView(view2, nativeResponse);
        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(2);

        ImpressionTrackingManager.removeView(view2);
        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(1);
        assertThat(ImpressionTrackingManager.getKeptViews().keySet()).containsOnly(view1);
    }

    @Test
    public void removeView_whenThereAreNoKeptViews_shouldDoNothing() throws Exception {
        assertThat(ImpressionTrackingManager.getKeptViews()).isEmpty();

        ImpressionTrackingManager.removeView(view);

        assertThat(ImpressionTrackingManager.getKeptViews()).isEmpty();
    }

    @Test
    public void removeView_whenViewIsNull_shouldDoNothing() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(1);
        ImpressionTrackingManager.removeView(null);
        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(1);
    }

    @Test
    public void visibilityCheckRun_whenWrapperIsNull_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getKeptViews().put(view, null);

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void visibilityCheckRun_whenNativeResponseIsNull_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getKeptViews().put(view, new NativeResponseWrapper(null));

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void visibilityCheckRun_whenNativeResponseHasRecordedImpression_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);
        nativeResponse.recordImpression(view);
        assertImpressionTracked(true);

        Robolectric.getFakeHttpLayer().clearRequestInfos();
        reset(mopubNativeListener);

        new VisibilityCheck().run();
        assertImpressionTracked(false);
    }

    @Test
    public void visibilityCheckRun_whenViewIsInvisible_shouldNotTrackImpression() throws Exception {
        view.setVisibility(View.INVISIBLE);
        ImpressionTrackingManager.addView(view, nativeResponse);

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsZero_shouldUpdateTimestampAndNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getKeptViews().get(view).mFirstVisibleTimestamp).isEqualTo(0);

        Robolectric.getUiThreadScheduler().advanceBy(111);
        new VisibilityCheck().run();

        assertThat(ImpressionTrackingManager.getKeptViews().get(view).mFirstVisibleTimestamp).isEqualTo(111);
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsNotZeroAndLessThanOneSecondHasElapsed_shouldNotTrackImpression() throws Exception {
        // Force the last viewed timestamp to be a known value
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getKeptViews().put(view, nativeResponseWrapper);

        // We progress 999 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 999);
        new VisibilityCheck().run();

        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Ignore("Review race condition")
    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsNotZeroAndMoreThanOneSecondHasElapsed_shouldTrackImpression() throws Exception {
        // Force the last viewed timestamp to be a known value
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getKeptViews().put(view, nativeResponseWrapper);

        // We progress 1000 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 1000);
        new VisibilityCheck().run();

        assertThat(nativeResponse.getRecordedImpression()).isTrue();
        assertImpressionTracked(true);
    }

    @Test
    public void isVisible_whenViewIsEntirelyOnScreen_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isTrue();
    }

    @Test
    public void isVisible_whenViewIs50PercentVisible_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 50, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isTrue();
    }

    @Test
    public void isVisible_whenViewIs49PercentVisible_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 49, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isFalse();
    }

    @Test
    public void isVisible_whenVisibleAreaIsZero_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 0, 0, 100, 100);

        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isFalse();
    }

    @Test
    public void isVisible_whenViewIsInvisibleOrGone_shouldReturnFalse() throws Exception {
        View view = getViewMock(View.INVISIBLE, 100, 100, 100, 100);
        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isFalse();

        reset(view);
        view = getViewMock(View.GONE, 100, 100, 100, 100);
        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isFalse();
    }

    @Test
    public void isVisible_whenViewHasZeroWidthAndHeight_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 0, 0);

        assertThat(VisibilityCheck.isVisible(view, nativeResponseWrapper)).isFalse();
    }

    @Test
    public void isVisible_whenViewOrNativeResponseWrapperIsNull_shouldReturnFalse() throws Exception {
        assertThat(VisibilityCheck.isVisible(null, nativeResponseWrapper)).isFalse();
        assertThat(VisibilityCheck.isVisible(view, null)).isFalse();
    }

    private View getViewMock(final int visibility,
                             final int visibleWidth, final int visibleHeight,
                             final int viewWidth, final int viewHeight) {
        View view = mock(View.class);
        when(view.getContext()).thenReturn(context);
        when(view.getVisibility()).thenReturn(visibility);
        when(view.getGlobalVisibleRect(any(Rect.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Object[] args = invocationOnMock.getArguments();
                        Rect rect = (Rect) args[0];
                        rect.set(0, 0, visibleWidth, visibleHeight);
                        return true;
                    }
                });
        when(view.getWidth()).thenReturn(viewWidth);
        when(view.getHeight()).thenReturn(viewHeight);

        return view;
    }

    private void assertImpressionTracked(final boolean wasTracked) {
        // Ensure that we fired off the HttpGets for each of the impression trackers
        if (wasTracked) {
            assertThat(Robolectric.getFakeHttpLayer().getSentHttpRequestInfos().size()).isEqualTo(1);
            final String actualUri = Robolectric.getFakeHttpLayer().getLastSentHttpRequestInfo().getHttpRequest().getRequestLine().getUri();
            assertThat(actualUri).isEqualTo(IMPRESSION_TRACKER);
            verify(mopubNativeListener).onNativeImpression(view);
        } else {
            assertThat(Robolectric.getFakeHttpLayer().getLastSentHttpRequestInfo()).isNull();
            verify(mopubNativeListener, never()).onNativeImpression(view);
        }
    }
}
