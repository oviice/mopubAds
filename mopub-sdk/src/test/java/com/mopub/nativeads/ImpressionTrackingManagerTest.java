package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.mopub.common.DownloadResponse;
import com.mopub.common.util.ResponseHeader;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import static android.view.ViewTreeObserver.OnPreDrawListener;
import static com.mopub.nativeads.ImpressionTrackingManager.NativeResponseWrapper;
import static com.mopub.nativeads.ImpressionTrackingManager.PollingRunnable;
import static com.mopub.nativeads.ImpressionTrackingManager.VisibilityChecker.isMostlyVisible;
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
    private static final int IMPRESSION_MIN_PERCENTAGE_VIEWED = 50;

    private View view;
    private NativeResponse nativeResponse;
    private NativeResponseWrapper nativeResponseWrapper;
    private Context context;
    private MoPubNativeListener mopubNativeListener;

    @Before
    public void setUp() throws Exception {
        ImpressionTrackingManager.clearTracking();

        context = new Activity();
        mopubNativeListener = mock(MoPubNativeListener.class);
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100, true, true);

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
        ImpressionTrackingManager.clearTracking();
    }

    @Test
    public void addView_withVisibleView_shouldAddViewToPollingHashMap() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews().keySet()).containsOnly(view);
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
    }

    @Test
    public void addView_withNonVisibleView_shouldAddViewToWaitingHashMap() throws Exception {
        view = getViewMock(View.GONE, 0, 0, 0, 0, true, false);

        ImpressionTrackingManager.addView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).containsOnly(view);
    }

    @Test
    public void addView_whenViewIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(null, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
    }

    @Test
    public void addView_whenNativeResponseIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(view, null);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
    }

    @Test
    public void isViewTracked_whenPollingViewsOrWaitingViewsContainsView_shouldReturnTrue() throws Exception {
        assertThat(ImpressionTrackingManager.isViewTracked(view)).isEqualTo(false);

        ImpressionTrackingManager.getPollingViews().put(view, new NativeResponseWrapper(nativeResponse));
        assertThat(ImpressionTrackingManager.isViewTracked(view)).isTrue();
        ImpressionTrackingManager.getPollingViews().clear();

        ImpressionTrackingManager.getWaitingViews().put(view, mock(OnPreDrawListener.class));
        assertThat(ImpressionTrackingManager.isViewTracked(view)).isTrue();
        ImpressionTrackingManager.getWaitingViews().clear();

        ImpressionTrackingManager.getPollingViews().put(view, new NativeResponseWrapper(nativeResponse));
        ImpressionTrackingManager.getWaitingViews().put(view, mock(OnPreDrawListener.class));
        assertThat(ImpressionTrackingManager.isViewTracked(view)).isTrue();
    }
    
    @Test
    public void waitForVisibility_shouldRemoveViewFromPollingHashMap() throws Exception {
        ImpressionTrackingManager.getPollingViews().put(view, new NativeResponseWrapper(nativeResponse));
        ImpressionTrackingManager.waitForVisibility(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
    }

    @Test
    public void waitForVisibility_whenViewTreeObserverIsAliveIsTrue_shouldAddOnPreDrawListenerToViewTreeObserverAndPopulateWaitingViewsHashMap() throws Exception {
        ViewTreeObserver mockViewTreeObserver = mock(ViewTreeObserver.class);
        when(mockViewTreeObserver.isAlive()).thenReturn(true);
        when(view.getViewTreeObserver()).thenReturn(mockViewTreeObserver);

        ImpressionTrackingManager.waitForVisibility(view, nativeResponse);

        verify(mockViewTreeObserver).addOnPreDrawListener(any(OnPreDrawListener.class));
        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).containsOnly(view);
    }

    @Test
    public void waitForVisibility_whenViewTreeObserverIsAliveIsFalse_shouldNotAddOnPreDrawListenerToViewTreeObserverAndNotPopulateWaitingViewsHashMap() throws Exception {
        ViewTreeObserver mockViewTreeObserver = mock(ViewTreeObserver.class);
        when(mockViewTreeObserver.isAlive()).thenReturn(false);
        when(view.getViewTreeObserver()).thenReturn(mockViewTreeObserver);

        ImpressionTrackingManager.waitForVisibility(view, nativeResponse);

        verify(mockViewTreeObserver, never()).addOnPreDrawListener(any(OnPreDrawListener.class));
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
    }

    @Test
    public void waitForVisibility_onPreDrawListener_onPreDraw_withVisibleView_shouldAddViewToPollingHashMap() throws Exception {
        ImpressionTrackingManager.waitForVisibility(view, nativeResponse);
        view.getViewTreeObserver().dispatchOnPreDraw();

        assertThat(ImpressionTrackingManager.getPollingViews().keySet()).containsOnly(view);
    }

    @Test
    public void waitForVisibility_onPreDrawListener_onPreDraw_withNonVisibleView_shouldNotAddViewToPollingHashMap() throws Exception {
        View view = getViewMock(View.INVISIBLE, 100, 100, 100, 100, true, true);
        ImpressionTrackingManager.waitForVisibility(view, nativeResponse);
        view.getViewTreeObserver().dispatchOnPreDraw();

        assertThat(ImpressionTrackingManager.getPollingViews().keySet()).isEmpty();
    }
    
    @Test
    public void pollVisibleView_shouldRemoveViewFromWaitingHashMap_shouldAddViewToPollingHashMap_shouldScheduleNextPoll() throws Exception {
        Robolectric.getUiThreadScheduler().pause();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        ImpressionTrackingManager.getWaitingViews().put(view, mock(OnPreDrawListener.class));

        ImpressionTrackingManager.pollVisibleView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(1);
        assertThat(ImpressionTrackingManager.getPollingViews().get(view)).isNotNull();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void scheduleNextPoll_withNoMessages_shouldSchedulePoll() throws Exception {
        Robolectric.getUiThreadScheduler().pause();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);

        ImpressionTrackingManager.scheduleNextPoll();

        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void removeWaiting_shouldRemoveViewFromWaitingHashMapAndRemoveOnPreDrawListener() throws Exception {
        ViewTreeObserver mockViewTreeObserver = mock(ViewTreeObserver.class);
        when(mockViewTreeObserver.isAlive()).thenReturn(true);
        when(view.getViewTreeObserver()).thenReturn(mockViewTreeObserver);

        OnPreDrawListener onPreDrawListener = mock(OnPreDrawListener.class);
        ImpressionTrackingManager.getWaitingViews().put(view, onPreDrawListener);

        ImpressionTrackingManager.removeWaitingView(view);

        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).isEmpty();
        verify(mockViewTreeObserver).removeOnPreDrawListener(onPreDrawListener);
    }

    @Test
    public void removeWaiting_withNullView_shouldDoNothing() throws Exception {
        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).isEmpty();

        ImpressionTrackingManager.removeWaitingView(null);

        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).isEmpty();
    }

    @Test
    public void removePolling_shouldRemoveViewFromPollingHashMap() throws Exception {
        ImpressionTrackingManager.getPollingViews().put(view, nativeResponseWrapper);

        ImpressionTrackingManager.removePollingView(view);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
    }

    @Test
    public void removePolling_withNullView_shouldDoNothing() throws Exception {
        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();

        ImpressionTrackingManager.removePollingView(null);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
    }

    @Test
    public void removeView_shouldRemoveViewFromWaitingAndPollingViews() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);
        View view1 = getViewMock(View.GONE, 0, 0, 0, 0, true, false);
        ImpressionTrackingManager.addView(view1, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(1);
        assertThat(ImpressionTrackingManager.getWaitingViews()).hasSize(1);

        ImpressionTrackingManager.removeView(view1);
        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(1);
       assertThat(ImpressionTrackingManager.getPollingViews().keySet()).containsOnly(view);
        assertThat(ImpressionTrackingManager.getWaitingViews()).hasSize(0);

        ImpressionTrackingManager.removeView(view);
        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(0);
        assertThat(ImpressionTrackingManager.getWaitingViews()).hasSize(0);
    }

    @Test
    public void removeView_withEmptyPollingAndWaitingHashMaps_shouldDoNothing() throws Exception {
        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();

        ImpressionTrackingManager.removeView(view);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
    }

    @Test
    public void removeView_whenViewIsNull_shouldDoNothing() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse);

        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(1);
        ImpressionTrackingManager.removeView(null);
        assertThat(ImpressionTrackingManager.getPollingViews()).hasSize(1);
    }

    @Test
    public void clearTracking_shouldClearWaitingAndPollingHashMaps_shouldClearHandlers() throws Exception {
        ImpressionTrackingManager.getWaitingViews().put(view, mock(OnPreDrawListener.class));
        ImpressionTrackingManager.getPollingViews().put(view, nativeResponseWrapper);

        ImpressionTrackingManager.clearTracking();

        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();

    }

    @Test
    public void pollingRunnableRun_whenWrapperIsNull_shouldNotTrackImpression() throws Exception {
        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getPollingViews().put(view, null);

        new PollingRunnable().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void pollingRunnableRun_whenNativeResponseIsNull_shouldNotTrackImpression() throws Exception {
        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getPollingViews().put(view, new NativeResponseWrapper(null));

        new PollingRunnable().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);
    }

    @Test
    public void pollingRunnableRun_whenNativeResponseHasRecordedImpression_shouldNotTrackImpression() throws Exception {
        nativeResponse.recordImpression(view);
        assertImpressionTracked(true);

        Robolectric.getFakeHttpLayer().clearRequestInfos();
        reset(mopubNativeListener);

        new PollingRunnable().run();
        assertImpressionTracked(false);
    }

    @Test
    public void pollingRunnableRun_whenNativeResponseHasBeenDestroyed_shouldNotTrackImpression() throws Exception {
        nativeResponse.destroy();

        Robolectric.getFakeHttpLayer().clearRequestInfos();
        reset(mopubNativeListener);

        new PollingRunnable().run();
        assertImpressionTracked(false);
    }

    @Test
    public void pollingRunnableRun_withNonVisibleView_shouldWaitForVisibility_shouldNotScheduleNextPoll() throws Exception {
        View view = getViewMock(View.INVISIBLE, 100, 100, 100, 100, true, true);
        ImpressionTrackingManager.getPollingViews().put(view, nativeResponseWrapper);

        new PollingRunnable().run();

        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews().keySet()).containsOnly(view);
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void pollingRunnableRun_whenLessThanOneSecondHasElapsed_shouldNotTrackImpression_shouldScheduleNextPoll() throws Exception {
        // Force the last viewed timestamp to be a known value
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getPollingViews().put(view, nativeResponseWrapper);

        // We progress 999 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 999);
        new PollingRunnable().run();

        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(false);

        assertThat(ImpressionTrackingManager.getPollingViews().keySet()).containsOnly(view);
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    @Test
    public void pollingRunnableRun_whenMoreThanOneSecondHasElapsed_shouldTrackImpression_shouldNotScheduleNextPoll() throws Exception {
        // Force the last viewed timestamp to be a known value
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getPollingViews().put(view, nativeResponseWrapper);

        // We progress 1000 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 1000);
        new PollingRunnable().run();

        assertThat(nativeResponse.getRecordedImpression()).isTrue();
        assertImpressionTracked(true);

        assertThat(ImpressionTrackingManager.getPollingViews()).isEmpty();
        assertThat(ImpressionTrackingManager.getWaitingViews()).isEmpty();
        assertThat(Robolectric.getUiThreadScheduler().enqueuedTaskCount()).isEqualTo(0);
    }

    @Test
    public void isMostlyVisible_whenParentIsNull_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100, false, true);
        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    @Test
    public void isMostlyVisible_whenViewIsOffScreen_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100, true, false);
        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }


    @Test
    public void isMostlyVisible_whenViewIsEntirelyOnScreen_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100, true, true);

        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isTrue();
    }

    @Test
    public void isMostlyVisible_whenViewIs50PercentVisible_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 50, 100, 100, 100, true, true);

        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isTrue();
    }

    @Test
    public void isMostlyVisible_whenViewIs49PercentVisible_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 49, 100, 100, 100, true, true);

        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    @Test
    public void isMostlyVisible_whenVisibleAreaIsZero_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 0, 0, 100, 100, true, true);

        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    @Test
    public void isMostlyVisible_whenViewIsInvisibleOrGone_shouldReturnFalse() throws Exception {
        View view = getViewMock(View.INVISIBLE, 100, 100, 100, 100, true, true);
        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();

        reset(view);
        view = getViewMock(View.GONE, 100, 100, 100, 100, true, true);
        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    @Test
    public void isMostlyVisible_whenViewHasZeroWidthAndHeight_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 0, 0, true, true);

        assertThat(isMostlyVisible(view, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    @Test
    public void isMostlyVisible_whenViewIsNull_shouldReturnFalse() throws Exception {
        assertThat(isMostlyVisible(null, IMPRESSION_MIN_PERCENTAGE_VIEWED)).isFalse();
    }

    static View getViewMock(final int visibility,
            final int visibleWidth, final int visibleHeight,
            final int viewWidth, final int viewHeight,
            final boolean isParentSet, final boolean isOnScreen) {
        View view = mock(View.class);
        when(view.getContext()).thenReturn(new Activity());
        when(view.getVisibility()).thenReturn(visibility);

        when(view.getGlobalVisibleRect(any(Rect.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                        Object[] args = invocationOnMock.getArguments();
                        Rect rect = (Rect) args[0];
                        rect.set(0, 0, visibleWidth, visibleHeight);
                        return isOnScreen;
                    }
                });

        when(view.getWidth()).thenReturn(viewWidth);
        when(view.getHeight()).thenReturn(viewHeight);
        if (isParentSet) {
            when(view.getParent()).thenReturn(mock(ViewParent.class));
        }

        when(view.getViewTreeObserver()).thenCallRealMethod();

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
