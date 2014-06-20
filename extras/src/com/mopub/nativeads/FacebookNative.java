package com.mopub.nativeads;

import android.content.Context;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Rating;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Tested with Facebook SDK 3.14.1
 */
public class FacebookNative extends CustomEventNative implements AdListener {
    private static final String PLACEMENT_ID_KEY = "placement_id";

    private Context mContext;
    private NativeAd mNativeAd;
    private CustomEventNativeListener mCustomEventNativeListener;

    // CustomEventNative implementation
    @Override
    protected void loadNativeAd(final Context context,
            final CustomEventNativeListener customEventNativeListener,
            final Map<String, Object> localExtras,
            final Map<String, String> serverExtras) {

        mContext = context.getApplicationContext();

        final String placementId;
        if (extrasAreValid(serverExtras)) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY);
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mCustomEventNativeListener = customEventNativeListener;

        mNativeAd = new NativeAd(context, placementId);
        mNativeAd.setAdListener(this);
        mNativeAd.loadAd();
    }

    // AdListener implementation
    @Override
    public void onAdLoaded(final Ad ad) {
        // This identity check is from Facebook's Native API sample code:
        // https://developers.facebook.com/docs/audience-network/android/native-api
        if (!mNativeAd.equals(ad) || !mNativeAd.isAdLoaded()) {
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
            return;
        }

        final FacebookForwardingNativeAd facebookForwardingNativeAd =
                new FacebookForwardingNativeAd(mNativeAd);

        final List<String> imageUrls = new ArrayList<String>();
        final String mainImageUrl = facebookForwardingNativeAd.getMainImageUrl();
        if (mainImageUrl != null) {
            imageUrls.add(facebookForwardingNativeAd.getMainImageUrl());
        }
        final String iconUrl = facebookForwardingNativeAd.getIconImageUrl();
        if (iconUrl != null) {
            imageUrls.add(facebookForwardingNativeAd.getIconImageUrl());
        }

        preCacheImages(mContext, imageUrls, new ImageListener() {
            @Override
            public void onImagesCached() {
                mCustomEventNativeListener.onNativeAdLoaded(facebookForwardingNativeAd);
            }

            @Override
            public void onImagesFailedToCache(NativeErrorCode errorCode) {
                mCustomEventNativeListener.onNativeAdFailed(errorCode);
            }
        });
    }

    @Override
    public void onError(final Ad ad, final AdError error) {
        if (error == AdError.NO_FILL) {
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
        } else if (error == AdError.INTERNAL_ERROR) {
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
        } else {
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
        }
    }

    @Override
    public void onAdClicked(final Ad ad) {
        // not used
    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

    static class FacebookForwardingNativeAd extends BaseForwardingNativeAd {
        private static final String SOCIAL_CONTEXT_FOR_AD = "socialContextForAd";
        private static final String APP_RATING_FOR_AD = "appRatingForAd";
        private static final int IMPRESSION_MIN_TIME_VIEWED = 0;

        private final NativeAd mNativeAd;

        FacebookForwardingNativeAd(final NativeAd nativeAd) {
            if (nativeAd == null) {
                throw new IllegalArgumentException("Facebook NativeAd cannot be null");
            }

            mNativeAd = nativeAd;

            setTitle(nativeAd.getAdTitle());
            setText(nativeAd.getAdBody());

            NativeAd.Image coverImage = nativeAd.getAdCoverImage();
            setMainImageUrl(coverImage == null ? null : coverImage.getUrl());

            NativeAd.Image icon = nativeAd.getAdIcon();
            setIconImageUrl(icon == null ? null : icon.getUrl());

            setCallToAction(nativeAd.getAdCallToAction());
            setStarRating(getDoubleRating(nativeAd.getAdStarRating()));

            addExtra(SOCIAL_CONTEXT_FOR_AD, nativeAd.getAdSocialContext());

            setImpressionMinTimeViewed(IMPRESSION_MIN_TIME_VIEWED);
        }

        @Override
        public void recordImpression() {
            mNativeAd.logImpression();
        }

        @Override
        public void handleClick(final View view) {
            mNativeAd.handleClick();
        }

        @Override
        public void destroy() {
            mNativeAd.destroy();
        }

        private static Double getDoubleRating(final Rating rating) {
            if (rating == null) {
                return null;
            }

            return MAX_STAR_RATING * rating.getValue() / rating.getScale();
        }
    }
}
