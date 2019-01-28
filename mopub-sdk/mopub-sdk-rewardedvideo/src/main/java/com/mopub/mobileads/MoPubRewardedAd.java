// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.DataKeys;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;
import com.mopub.common.Preconditions;
import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.util.Map;

import static com.mopub.common.Constants.AD_EXPIRATION_DELAY;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.EXPIRED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_FAILED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.WILL_DISAPPEAR;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.WILL_LEAVE_APPLICATION;

/**
 * Contains the common logic for rewarded ads.
 */
public abstract class MoPubRewardedAd extends CustomEventRewardedAd {

    private boolean mIsLoaded;
    @Nullable private String mRewardedAdCurrencyName;
    private int mRewardedAdCurrencyAmount;
    @Nullable protected String mAdUnitId;

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        // This custom event does not need additional lifecycle listeners.
        return null;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull final Activity launcherActivity,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final Map<String, String> serverExtras) throws Exception {
        // No additional initialization is necessary.
        return false;
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull final Activity activity,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final Map<String, String> serverExtras) throws Exception {
        Preconditions.checkNotNull(activity, "activity cannot be null");
        Preconditions.checkNotNull(localExtras, "localExtras cannot be null");
        Preconditions.checkNotNull(serverExtras, "serverExtras cannot be null");

        final Object rewardedAdCurrencyName = localExtras.get(
                DataKeys.REWARDED_AD_CURRENCY_NAME_KEY);
        if (rewardedAdCurrencyName instanceof String) {
            mRewardedAdCurrencyName = (String) rewardedAdCurrencyName;
        } else {
            MoPubLog.log(CUSTOM, "No currency name specified for rewarded video. Using the default name.");
            mRewardedAdCurrencyName = MoPubReward.NO_REWARD_LABEL;
        }

        final Object rewardedAdCurrencyAmount = localExtras.get(
                DataKeys.REWARDED_AD_CURRENCY_AMOUNT_STRING_KEY);
        if (rewardedAdCurrencyAmount instanceof String) {
            try {
                mRewardedAdCurrencyAmount = Integer.parseInt(
                        (String) rewardedAdCurrencyAmount);
            } catch (NumberFormatException e) {
                MoPubLog.log(CUSTOM,
                        "Unable to convert currency amount: " + rewardedAdCurrencyAmount +
                                ". Using the default reward amount: " +
                                MoPubReward.DEFAULT_REWARD_AMOUNT);
                mRewardedAdCurrencyAmount = MoPubReward.DEFAULT_REWARD_AMOUNT;
            }
        } else {
            MoPubLog.log(CUSTOM,
                    "No currency amount specified for rewarded ad. Using the default reward amount: " +
                            MoPubReward.DEFAULT_REWARD_AMOUNT);
            mRewardedAdCurrencyAmount = MoPubReward.DEFAULT_REWARD_AMOUNT;
        }

        if (mRewardedAdCurrencyAmount < 0) {
            MoPubLog.log(CUSTOM,
                    "Negative currency amount specified for rewarded ad. Using the default reward amount: " +
                            MoPubReward.DEFAULT_REWARD_AMOUNT);
            mRewardedAdCurrencyAmount = MoPubReward.DEFAULT_REWARD_AMOUNT;
        }

        final Object adUnitId = localExtras.get(DataKeys.AD_UNIT_ID_KEY);
        if (adUnitId instanceof String) {
            mAdUnitId = (String) adUnitId;
        } else {
            MoPubLog.log(CUSTOM, "Unable to set ad unit for rewarded ad.");
        }
    }

    @Override
    protected void onInvalidate() {
        mIsLoaded = false;
    }

    @Override
    protected boolean isReady() {
        return mIsLoaded;
    }

    protected class MoPubRewardedAdListener implements CustomEventInterstitial.CustomEventInterstitialListener {

        @NonNull final Class<? extends MoPubRewardedAd>  mCustomEventClass;

        @NonNull private final Runnable mAdExpiration;
        @NonNull private Handler mHandler;

        public MoPubRewardedAdListener(@NonNull final Class<? extends MoPubRewardedAd>
                customEventClass) {
            Preconditions.checkNotNull(customEventClass);

            mCustomEventClass = customEventClass;

            mHandler = new Handler();
            mAdExpiration = new Runnable() {
                @Override
                public void run() {
                    MoPubLog.log(EXPIRED, "time in seconds");
                    onInterstitialFailed(MoPubErrorCode.EXPIRED);
                }
            };

        }

        @Override
        public void onInterstitialLoaded() {
            MoPubLog.log(LOAD_SUCCESS);
            mIsLoaded = true;
            // Expire MoPub ads to synchronize with MoPub Ad Server tracking window
            if (AdTypeTranslator.CustomEventType.isMoPubSpecific(mCustomEventClass.getName())) {
                mHandler.postDelayed(mAdExpiration, AD_EXPIRATION_DELAY);
            }
            MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(mCustomEventClass,
                    getAdNetworkId());
        }

        @Override
        public void onInterstitialFailed(final MoPubErrorCode errorCode) {
            MoPubLog.log(SHOW_FAILED, errorCode.getIntCode(), errorCode);
            mHandler.removeCallbacks(mAdExpiration);
            switch (errorCode) {
                case VIDEO_PLAYBACK_ERROR:
                    MoPubRewardedVideoManager.onRewardedVideoPlaybackError(mCustomEventClass,
                            getAdNetworkId(), errorCode);
                    break;
                default:
                    MoPubRewardedVideoManager.onRewardedVideoLoadFailure(mCustomEventClass,
                            getAdNetworkId(), errorCode);
            }
        }

        @Override
        public void onInterstitialShown() {
            MoPubLog.log(SHOW_SUCCESS);
            mHandler.removeCallbacks(mAdExpiration);
            MoPubRewardedVideoManager.onRewardedVideoStarted(mCustomEventClass, getAdNetworkId());
        }

        @Override
        public void onInterstitialClicked() {
            MoPubLog.log(CLICKED);
            MoPubRewardedVideoManager.onRewardedVideoClicked(mCustomEventClass, getAdNetworkId());
        }

        @Override
        public void onInterstitialImpression() {
        }

        @Override
        public void onLeaveApplication() {
            MoPubLog.log(WILL_LEAVE_APPLICATION);
        }

        @Override
        public void onInterstitialDismissed() {
            MoPubLog.log(WILL_DISAPPEAR);
            MoPubRewardedVideoManager.onRewardedVideoClosed(mCustomEventClass, getAdNetworkId());
            onInvalidate();
        }

        @Deprecated
        @VisibleForTesting
        void setHandler(@NonNull final Handler handler) {
            mHandler = handler;
        }
    }

    @Nullable
    protected String getRewardedAdCurrencyName() {
        return mRewardedAdCurrencyName;
    }

    protected int getRewardedAdCurrencyAmount() {
        return mRewardedAdCurrencyAmount;
    }

    @Deprecated
    @VisibleForTesting
    void setIsLoaded(final boolean isLoaded) {
        mIsLoaded = isLoaded;
    }

    @Deprecated
    @VisibleForTesting
    MoPubRewardedAdListener createListener(@NonNull final Class<? extends MoPubRewardedAd>
            customEventClass) {
        return new MoPubRewardedAdListener(customEventClass);
    }

}
