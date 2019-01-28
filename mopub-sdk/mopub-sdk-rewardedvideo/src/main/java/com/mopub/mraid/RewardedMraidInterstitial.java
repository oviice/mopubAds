// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mraid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.RewardedMraidActivity;

import java.util.Map;

import static com.mopub.common.DataKeys.REWARDED_AD_DURATION_KEY;
import static com.mopub.common.DataKeys.SHOULD_REWARD_ON_CLICK_KEY;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;

/**
 * Handles the showing of rewarded MRAID interstitials. 'Rewarded duration' and 'should reward on
 * click' are optional and will default to the values set in {@link RewardedMraidController}.
 */
public class RewardedMraidInterstitial extends MraidInterstitial {
    public static final String ADAPTER_NAME = RewardedMraidInterstitial.class.getSimpleName();

    public interface RewardedMraidInterstitialListener extends CustomEventInterstitial.CustomEventInterstitialListener {
        void onMraidComplete();
    }

    @Nullable private RewardedPlayableBroadcastReceiver mRewardedPlayableBroadcastReceiver;
    private int mRewardedDuration;
    private boolean mShouldRewardOnClick;

    @Override
    public void loadInterstitial(
            Context context,
            CustomEventInterstitialListener customEventInterstitialListener,
            Map<String, Object> localExtras,
            Map<String, String> serverExtras) {
        super.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        MoPubLog.log(LOAD_ATTEMPTED, ADAPTER_NAME);

        if (customEventInterstitialListener instanceof RewardedMraidInterstitialListener) {
            mRewardedPlayableBroadcastReceiver = new RewardedPlayableBroadcastReceiver(
                    (RewardedMraidInterstitialListener) customEventInterstitialListener,
                    mBroadcastIdentifier);
            mRewardedPlayableBroadcastReceiver.register(mRewardedPlayableBroadcastReceiver, context);
        }


    }

    @Override
    protected void preRenderHtml(@NonNull CustomEventInterstitialListener
            customEventInterstitialListener) {
        final Map<String, Object> localExtras = mLocalExtras;

        if (localExtras != null) {
            final Object rewardedDurationObject = localExtras.get(REWARDED_AD_DURATION_KEY);
            mRewardedDuration = rewardedDurationObject instanceof Integer ?
                    (int) rewardedDurationObject :
                    RewardedMraidController.DEFAULT_PLAYABLE_DURATION_FOR_CLOSE_BUTTON_SECONDS;
            final Object shouldRewardOnClickObject = localExtras.get(SHOULD_REWARD_ON_CLICK_KEY);
            mShouldRewardOnClick = shouldRewardOnClickObject instanceof Boolean ?
                    (boolean) shouldRewardOnClickObject :
                    RewardedMraidController.DEFAULT_PLAYABLE_SHOULD_REWARD_ON_CLICK;

        }
        RewardedMraidActivity.preRenderHtml(this, mContext,
                customEventInterstitialListener, mHtmlData, mBroadcastIdentifier, mAdReport,
                mRewardedDuration);
    }

    @Override
    public void showInterstitial() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        RewardedMraidActivity.start(mContext, mAdReport, mHtmlData, mBroadcastIdentifier,
                mRewardedDuration, mShouldRewardOnClick);
    }


    @Override
    public void onInvalidate() {
        super.onInvalidate();
        if (mRewardedPlayableBroadcastReceiver != null) {
            mRewardedPlayableBroadcastReceiver.unregister(mRewardedPlayableBroadcastReceiver);
        }
    }

    @Deprecated
    @VisibleForTesting
    int getRewardedDuration() {
        return mRewardedDuration;
    }

    @Deprecated
    @VisibleForTesting
    boolean isShouldRewardOnClick() {
        return mShouldRewardOnClick;
    }
}
