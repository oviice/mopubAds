package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import com.mopub.nativeads.test.support.SdkTestRunner;

import org.json.JSONArray;
import org.json.JSONObject;
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
    private Context context;
    private MoPubNativeListener mopubNativeListener;
    private JSONObject fakeJsonObject;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        mopubNativeListener = mock(MoPubNativeListener.class);
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100);
        fakeJsonObject = new JSONObject();
        fakeJsonObject.put("imptracker", new JSONArray("[\"url1\", \"url2\"]"));
        fakeJsonObject.put("clktracker", "expected clicktracker");
        nativeResponse = new NativeResponse(fakeJsonObject);
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
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).hasSize(1);
        assertThat(keptViews.get(view).mNativeResponse).isEqualTo(nativeResponse);
    }

    @Test
    public void addView_whenViewIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(null, nativeResponse, mopubNativeListener);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).isEmpty();
    }

    @Test
    public void addView_whenNativeResponseIsNull_shouldNotAddView() throws Exception {
        ImpressionTrackingManager.addView(view, null, mopubNativeListener);
        Map<View, NativeResponseWrapper> keptViews = ImpressionTrackingManager.getKeptViews();
        assertThat(keptViews).isEmpty();
    }

    @Test
    public void removeView_shouldRemoveViewFromKeptViews() throws Exception {
        View view1 = mock(View.class);
        View view2 = mock(View.class);

        ImpressionTrackingManager.addView(view1, nativeResponse, mopubNativeListener);
        ImpressionTrackingManager.addView(view2, nativeResponse, mopubNativeListener);
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
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);

        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(1);
        ImpressionTrackingManager.removeView(null);
        assertThat(ImpressionTrackingManager.getKeptViews()).hasSize(1);
    }

    @Test
    public void visibilityCheckRun_whenWrapperIsNull_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);

        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getKeptViews().put(view, null);

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(nativeResponse, false);
    }

    @Test
    public void visibilityCheckRun_whenNativeResponseIsNull_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);

        // This doesn't normally happen; perhaps we're being overly defensive
        ImpressionTrackingManager.getKeptViews().put(view, new NativeResponseWrapper(null, null));

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(nativeResponse, false);
    }

    @Test
    public void visibilityCheckRun_whenNativeResponseHasRecordedImpression_shouldNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);
        nativeResponse.recordImpression();
        assertThat(nativeResponse.getRecordedImpression()).isTrue();

        new VisibilityCheck().run();
        assertImpressionTracked(nativeResponse, false);
    }

    @Test
    public void visibilityCheckRun_whenViewIsInvisible_shouldNotTrackImpression() throws Exception {
        view.setVisibility(View.INVISIBLE);
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);

        new VisibilityCheck().run();
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(nativeResponse, false);
    }

    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsZero_shouldUpdateTimestampAndNotTrackImpression() throws Exception {
        ImpressionTrackingManager.addView(view, nativeResponse, mopubNativeListener);

        assertThat(ImpressionTrackingManager.getKeptViews().get(view).mFirstVisibleTimestamp).isEqualTo(0);

        Robolectric.getUiThreadScheduler().advanceBy(111);
        new VisibilityCheck().run();

        assertThat(ImpressionTrackingManager.getKeptViews().get(view).mFirstVisibleTimestamp).isEqualTo(111);
        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(nativeResponse, false);
    }

    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsNotZeroAndLessThanOneSecondHasElapsed_shouldNotTrackImpression() throws Exception {
        // Force the last viewed timestamp to be a known value
        NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper(nativeResponse, mopubNativeListener);
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getKeptViews().put(view, nativeResponseWrapper);

        // We progress 999 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 999);
        new VisibilityCheck().run();

        assertThat(nativeResponse.getRecordedImpression()).isFalse();
        assertImpressionTracked(nativeResponse, false);
    }

    @Ignore("Review race condition")
    @Test
    public void visibilityCheckRun_whenLastViewedTimestampIsNotZeroAndMoreThanOneSecondHasElapsed_shouldTrackImpression() throws Exception {
        // Force the last viewed timestamp to be a known value
        NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper(nativeResponse, mopubNativeListener);
        nativeResponseWrapper.mFirstVisibleTimestamp = 5555;
        ImpressionTrackingManager.getKeptViews().put(view, nativeResponseWrapper);

        // We progress 1000 milliseconds
        Robolectric.getUiThreadScheduler().advanceBy(5555 + 1000);
        new VisibilityCheck().run();

        assertThat(nativeResponse.getRecordedImpression()).isTrue();
        assertImpressionTracked(nativeResponse, true);
    }

    @Test
    public void isVisible_whenViewIsEntirelyOnScreen_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view)).isTrue();
    }

    @Test
    public void isVisible_whenViewIs50PercentVisible_shouldReturnTrue() throws Exception {
        view = getViewMock(View.VISIBLE, 50, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view)).isTrue();
    }

    @Test
    public void isVisible_whenViewIs49PercentVisible_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 49, 100, 100, 100);

        assertThat(VisibilityCheck.isVisible(view)).isFalse();
    }

    @Test
    public void isVisible_whenVisibleAreaIsZero_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 0, 0, 100, 100);

        assertThat(VisibilityCheck.isVisible(view)).isFalse();
    }

    @Test
    public void isVisible_whenViewIsInvisibleOrGone_shouldReturnFalse() throws Exception {
        View view = getViewMock(View.INVISIBLE, 100, 100, 100, 100);
        assertThat(VisibilityCheck.isVisible(view)).isFalse();

        reset(view);
        view = getViewMock(View.GONE, 100, 100, 100, 100);
        assertThat(VisibilityCheck.isVisible(view)).isFalse();
    }

    @Test
    public void isVisible_whenViewHasZeroWidthAndHeight_shouldReturnFalse() throws Exception {
        view = getViewMock(View.VISIBLE, 100, 100, 0, 0);

        assertThat(VisibilityCheck.isVisible(view)).isFalse();
    }

    @Test
    public void isVisible_whenViewIsNull_shouldReturnFalse() throws Exception {
        assertThat(VisibilityCheck.isVisible(null)).isFalse();
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

    private void assertImpressionTracked(final NativeResponse nativeResponseMock, final boolean wasTracked) {
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
