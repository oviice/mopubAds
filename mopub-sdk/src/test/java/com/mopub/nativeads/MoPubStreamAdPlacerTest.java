package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.NativeAdSource.AdSourceListener;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class MoPubStreamAdPlacerTest {
    private Context context;

    PlacementData placementData;

    @Mock
    NativeAdSource mockAdSource;
    @Mock
    MoPubNativeAdRenderer mockAdRenderer;
    @Mock
    MoPubNativeAdLoadedListener mockAdLoadedListener;
    @Mock
    ImpressionTracker mockImpressionTracker;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    NativeResponse stubNativeResponse;

    private MoPubStreamAdPlacer subject;

    @Before
    public void setup() {
        context = new Activity();

        // Repeating every 5 positions
        placementData = PlacementData.fromAdPositioning(MoPubNativeAdPositioning.newBuilder()
                .enableRepeatingPositions(2)
                .build());

        subject = new MoPubStreamAdPlacer(context, mockAdSource, mockImpressionTracker, placementData);
        subject.registerAdRenderer(mockAdRenderer);
        subject.setAdLoadedListener(mockAdLoadedListener);
    }

    @Test
    public void isAd_initialState_hasNoAds() {
        assertThat(subject.isAd(0)).isFalse();
        assertThat(subject.isAd(1)).isFalse();
        assertThat(subject.isAd(2)).isFalse();
        assertThat(subject.isAd(3)).isFalse();
        assertThat(subject.isAd(4)).isFalse();
    }

    @Test
    public void isAd_placeAdsWithNoAdsAvailable_hasNoAds() {
        subject.setItemCount(4);

        assertThat(subject.isAd(0)).isFalse();
        assertThat(subject.isAd(1)).isFalse();
        assertThat(subject.isAd(2)).isFalse();
        assertThat(subject.isAd(3)).isFalse();
        assertThat(subject.isAd(4)).isFalse();
    }

    @Test
    public void isAd_placeAdsWithAdsAvailable_hasAds() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.isAd(0)).isFalse();
        assertThat(subject.isAd(1)).isTrue();
        assertThat(subject.isAd(2)).isFalse();
        assertThat(subject.isAd(3)).isTrue();
        assertThat(subject.isAd(4)).isFalse();
    }

    @Test
    public void getAdData_placeAdsWithAdsAvailable_hasAds() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.getAdData(0)).isNull();
        assertThat(subject.getAdData(1)).isNotNull();
        assertThat(subject.getAdData(2)).isNull();
        assertThat(subject.getAdData(3)).isNotNull();
        assertThat(subject.getAdData(4)).isNull();
    }

    @Test
    public void getOriginalPosition_adjustsPositions() {
        assertThat(subject.getOriginalPosition(0)).isEqualTo(0);
        assertThat(subject.getOriginalPosition(1)).isEqualTo(1);
        assertThat(subject.getOriginalPosition(2)).isEqualTo(2);
        assertThat(subject.getOriginalPosition(3)).isEqualTo(3);
        assertThat(subject.getOriginalPosition(4)).isEqualTo(4);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.getOriginalPosition(0)).isEqualTo(0);
        assertThat(subject.getOriginalPosition(1)).isEqualTo(PlacementData.NOT_FOUND);
        assertThat(subject.getOriginalPosition(2)).isEqualTo(1);
        assertThat(subject.getOriginalPosition(3)).isEqualTo(PlacementData.NOT_FOUND);
        assertThat(subject.getOriginalPosition(4)).isEqualTo(2);
    }

    @Test
    public void getAdjustedPosition_adjustsPositions() {
        assertThat(subject.getAdjustedPosition(0)).isEqualTo(0);
        assertThat(subject.getAdjustedPosition(1)).isEqualTo(1);
        assertThat(subject.getAdjustedPosition(2)).isEqualTo(2);
        assertThat(subject.getAdjustedPosition(3)).isEqualTo(3);
        assertThat(subject.getAdjustedPosition(4)).isEqualTo(4);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.getAdjustedPosition(0)).isEqualTo(0);
        assertThat(subject.getAdjustedPosition(1)).isEqualTo(2);
        assertThat(subject.getAdjustedPosition(2)).isEqualTo(4);
        assertThat(subject.getAdjustedPosition(3)).isEqualTo(6);
        assertThat(subject.getAdjustedPosition(4)).isEqualTo(7);
    }

    @Test
    public void getOriginalCount_adjustsPositions() {
        assertThat(subject.getOriginalCount(0)).isEqualTo(0);
        assertThat(subject.getOriginalCount(1)).isEqualTo(1);
        assertThat(subject.getOriginalCount(2)).isEqualTo(2);
        assertThat(subject.getOriginalCount(3)).isEqualTo(3);
        assertThat(subject.getOriginalCount(4)).isEqualTo(4);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.getOriginalCount(0)).isEqualTo(0);
        assertThat(subject.getOriginalCount(1)).isEqualTo(1);
        assertThat(subject.getOriginalCount(2)).isEqualTo(PlacementData.NOT_FOUND);
        assertThat(subject.getOriginalCount(3)).isEqualTo(2);
        assertThat(subject.getOriginalCount(4)).isEqualTo(PlacementData.NOT_FOUND);
    }

    @Test
    public void getAdjustedCount_adjustsPositions() {
        assertThat(subject.getAdjustedCount(0)).isEqualTo(0);
        assertThat(subject.getAdjustedCount(1)).isEqualTo(1);
        assertThat(subject.getAdjustedCount(2)).isEqualTo(2);
        assertThat(subject.getAdjustedCount(3)).isEqualTo(3);
        assertThat(subject.getAdjustedCount(4)).isEqualTo(4);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5

        assertThat(subject.getAdjustedCount(0)).isEqualTo(0);
        assertThat(subject.getAdjustedCount(1)).isEqualTo(1);
        assertThat(subject.getAdjustedCount(2)).isEqualTo(3);
        assertThat(subject.getAdjustedCount(3)).isEqualTo(5);
        assertThat(subject.getAdjustedCount(4)).isEqualTo(7);
    }

    @Test
    public void placeAds_shouldCallListener() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);

        subject.setItemCount(4);  // Will place ads at positions 1, 3, and 5
        verify(mockAdLoadedListener, never()).onAdLoaded(0);
        verify(mockAdLoadedListener).onAdLoaded(1);
        verify(mockAdLoadedListener, never()).onAdLoaded(2);
        verify(mockAdLoadedListener).onAdLoaded(3);
        verify(mockAdLoadedListener, never()).onAdLoaded(4);
        verify(mockAdLoadedListener).onAdLoaded(5);
        verify(mockAdLoadedListener, never()).onAdLoaded(6);
        verify(mockAdLoadedListener, never()).onAdLoaded(7);
    }

    @Test
    public void placeAdsInRange_shouldPlaceAfter() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);

        Robolectric.getUiThreadScheduler().pause();
        subject.setItemCount(100);
        subject.placeAdsInRange(50, 50);
        Robolectric.getUiThreadScheduler().advanceToLastPostedRunnable();

        assertThat(subject.isAd(48)).isFalse();
        assertThat(subject.isAd(49)).isFalse();
        assertThat(subject.isAd(50)).isTrue();
        assertThat(subject.isAd(51)).isFalse();
        assertThat(subject.isAd(52)).isTrue();
        assertThat(subject.isAd(53)).isFalse();
        assertThat(subject.isAd(54)).isTrue();
    }

    @Test
    public void placeAdsInRange_shouldCallListener() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);

        Robolectric.getUiThreadScheduler().pause();
        subject.setItemCount(100);
        subject.placeAdsInRange(50, 54);
        Robolectric.getUiThreadScheduler().advanceToLastPostedRunnable();

        verify(mockAdLoadedListener).onAdLoaded(50);
        verify(mockAdLoadedListener, never()).onAdLoaded(51);
        verify(mockAdLoadedListener).onAdLoaded(52);
        verify(mockAdLoadedListener, never()).onAdLoaded(53);
        verify(mockAdLoadedListener).onAdLoaded(54);
        verify(mockAdLoadedListener, never()).onAdLoaded(55);
        verify(mockAdLoadedListener).onAdLoaded(56);
    }

    @Test
    public void placeAdsInRange_aboveItemCount_shouldNotInsert() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);

        Robolectric.getUiThreadScheduler().pause();
        subject.setItemCount(0);
        subject.placeAdsInRange(50, 54);
        Robolectric.getUiThreadScheduler().advanceToLastPostedRunnable();

        verify(mockAdLoadedListener, never()).onAdLoaded(50);
    }

    @Test
    public void getAdView_withNoAds_returnsNull() {
        assertThat(subject.getAdView(1, null, null)).isNull();
    }

    @Test
    public void loadAds_shouldLoadAdsWhenAvailable() {
        // Shouldn't load ads because there aren't any available
        subject.setItemCount(2);
        subject.placeAdsInRange(0, 1);
        subject.loadAds("test-ad-unit-id");
        assertThat(subject.isAd(1)).isFalse();
        verify(mockAdLoadedListener, never()).onAdLoaded(anyInt());

        // Capture the ad source listener so that we can trigger an ad loading
        ArgumentCaptor<AdSourceListener> captor = ArgumentCaptor.forClass(AdSourceListener.class);
        verify(mockAdSource).setAdSourceListener(captor.capture());
        AdSourceListener adSourceListener = captor.getValue();
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        adSourceListener.onAdsAvailable();

        // Check that an ad was loaded
        assertThat(subject.isAd(1)).isTrue();
        verify(mockAdLoadedListener).onAdLoaded(1);
    }

    @Test
    public void loadAds_shouldClearAds_afterFirstAdLoads() {
        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(2);
        subject.placeAdsInRange(0, 1);

        subject.loadAds("test-ad-unit-id");
        ArgumentCaptor<AdSourceListener> captor = ArgumentCaptor.forClass(AdSourceListener.class);
        verify(mockAdSource).setAdSourceListener(captor.capture());
        AdSourceListener adSourceListener = captor.getValue();

        // Ad should still exist until a new ad is available
        assertThat(subject.isAd(1)).isTrue();
        verify(mockAdLoadedListener, never()).onAdRemoved(anyInt());

        // Once an ad is available, it should be immediately removed and replaced
        adSourceListener.onAdsAvailable();
        verify(mockAdLoadedListener).onAdRemoved(1);
        verify(mockAdLoadedListener, times(2)).onAdLoaded(1);
        assertThat(subject.isAd(1)).isTrue();
    }

    @Test
    public void clearAds_shouldClearAdSource_shouldClearImpressionTracker_shouldResetPlacementData() {
        PlacementData mockPlacementData = mock(PlacementData.class);
        subject = new MoPubStreamAdPlacer(context, mockAdSource, mockImpressionTracker, mockPlacementData);

        subject.destroy();

        verify(mockAdSource).clear();
        verify(mockImpressionTracker).destroy();
        verify(mockPlacementData).clearAds();
    }

    @Test
    public void getAdView_withNullConvertView_callsRenderer_addsToImpressionTracker() {
        View view = new View(context);
        when(mockAdRenderer.createAdView(any(Context.class), any(ViewGroup.class)))
                .thenReturn(view);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(100);

        assertThat(subject.getAdView(1, null, null)).isEqualTo(view);
        verify(mockAdRenderer).createAdView(any(Context.class), any(ViewGroup.class));
        verify(mockAdRenderer).renderAdView(view, stubNativeResponse);
        verify(mockImpressionTracker).addView(view, stubNativeResponse);
    }

    @Test
    public void getAdView_withConvertView_callsRenderer_addsToImpressionTracker() {
        View convertView = new View(context);

        when(mockAdSource.dequeueAd()).thenReturn(stubNativeResponse);
        subject.setItemCount(100);

        assertThat(subject.getAdView(1, convertView, null)).isEqualTo(convertView);
        verify(mockAdRenderer, never()).createAdView(any(Context.class), any(ViewGroup.class));
        verify(mockAdRenderer).renderAdView(convertView, stubNativeResponse);
        verify(mockImpressionTracker).addView(convertView, stubNativeResponse);
    }

    @Test
    public void destroy_shouldClearAdSource_shouldDestroyImpressionTracker_shouldResetPlacementData() {
        PlacementData mockPlacementData = mock(PlacementData.class);
        subject = new MoPubStreamAdPlacer(context, mockAdSource, mockImpressionTracker, mockPlacementData);

        subject.destroy();

        verify(mockAdSource).clear();
        verify(mockImpressionTracker).destroy();
        verify(mockPlacementData).clearAds();
    }
}
