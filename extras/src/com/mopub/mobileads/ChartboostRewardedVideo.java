package com.mopub.mobileads;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.mopub.common.DataKeys;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPubReward;
import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.util.*;

import static com.mopub.mobileads.MoPubErrorCode.VIDEO_DOWNLOAD_ERROR;

/**
 * A custom event for showing Chartboost rewarded videos.
 *
 * Certified with Chartboost 5.0.4
 */
public class ChartboostRewardedVideo extends CustomEventRewardedVideo {
    public static final String APP_ID_KEY = "appId";
    public static final String APP_SIGNATURE_KEY = "appSignature";
    public static final String LOCATION_KEY = "location";
    public static final String LOCATION_DEFAULT = "Default";

    @NonNull private static final SingletonChartboostDelegate sSingletonChartboostDelegate =
            new SingletonChartboostDelegate();
    @NonNull private static final LifecycleListener sLifecycleListener =
            new ChartboostLifecycleListener();
    private static boolean sInitialized = false;

    @NonNull private String mLocation = LOCATION_DEFAULT;
    @NonNull private final Handler mHandler;

    public ChartboostRewardedVideo() {
        mHandler = new Handler();
    }

    @Override
    @NonNull
    public CustomEventRewardedVideoListener getVideoListenerForSdk() {
        return sSingletonChartboostDelegate;
    }

    @Override
    @NonNull
    public LifecycleListener getLifecycleListener() {
        return sLifecycleListener;
    }

    @Override
    @NonNull
    public String getAdNetworkId() {
        return mLocation;
    }

    @Override
    public boolean checkAndInitializeSdk(@NonNull Activity launcherActivity,
            @NonNull Map<String, Object> localExtras,
            @NonNull Map<String, String> serverExtras) throws Exception {
        synchronized (ChartboostRewardedVideo.class) {
            if (sInitialized) {
                return false;
            }

            if (!serverExtras.containsKey(APP_ID_KEY)) {
                throw new IllegalStateException("Chartboost rewarded video initialization" +
                        " failed due to missing application ID.");
            }

            if (!serverExtras.containsKey(APP_SIGNATURE_KEY)) {
                throw new IllegalStateException("Chartboost rewarded video initialization" +
                        " failed due to missing application signature.");
            }

            final String appId = serverExtras.get(APP_ID_KEY);
            final String appSignature = serverExtras.get(APP_SIGNATURE_KEY);

            Chartboost.startWithAppId(launcherActivity, appId, appSignature);
            Chartboost.setDelegate(sSingletonChartboostDelegate);

            sInitialized = true;
            return true;
        }
    }

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity,
            @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras)
            throws Exception {
        if (serverExtras.containsKey(LOCATION_KEY)) {
            mLocation = serverExtras.get(LOCATION_KEY);
        } else {
            mLocation = LOCATION_DEFAULT;
        }

        sSingletonChartboostDelegate.mLocationsToLoad.add(mLocation);
        setUpMediationSettingsForRequest((String) localExtras.get(DataKeys.AD_UNIT_ID_KEY));

        // We do this to ensure that the custom event manager has a chance to get the listener
        // and ad unit ID before and delegate callbacks are made.
        mHandler.post(new Runnable() {
            public void run() {
                Chartboost.cacheRewardedVideo(mLocation);
            }
        });
    }

    private void setUpMediationSettingsForRequest(String moPubId) {
        final ChartboostMediationSettings globalSettings =
                MoPubRewardedVideoManager.getGlobalMediationSettings(ChartboostMediationSettings.class);
        final ChartboostMediationSettings instanceSettings =
                MoPubRewardedVideoManager.getInstanceMediationSettings(ChartboostMediationSettings.class, moPubId);

        // Instance settings override global settings.
        if (instanceSettings != null) {
            Chartboost.setCustomId(instanceSettings.getCustomId());
        } else if (globalSettings != null) {
            Chartboost.setCustomId(globalSettings.getCustomId());
        }
    }

    @Override
    public boolean hasVideoAvailable() {
        return Chartboost.hasRewardedVideo(mLocation);
    }

    @Override
    public void showVideo() {
        if (hasVideoAvailable()) {
            Chartboost.showRewardedVideo(mLocation);
        } else {
            MoPubLog.d("Attempted to show Chartboost rewarded video before it was available.");
        }
    }

    @Override
    protected void onInvalidate() {
        // This prevents sending didCache or didFailToCache callbacks.
        sSingletonChartboostDelegate.mLocationsToLoad.remove(mLocation);
    }

    private static final class SingletonChartboostDelegate extends ChartboostDelegate
            implements CustomEventRewardedVideoListener {

        private Set<String> mLocationsToLoad = Collections.synchronizedSet(new TreeSet<String>());

        @Override
        public boolean shouldDisplayRewardedVideo(String location) {
            return super.shouldDisplayRewardedVideo(location);
        }

        @Override
        public void didCacheRewardedVideo(String location) {
            super.didCacheRewardedVideo(location);

            if (mLocationsToLoad.contains(location)) {
                MoPubLog.d("Chartboost rewarded video cached for location " + location + ".");
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(ChartboostRewardedVideo.class, location);
                mLocationsToLoad.remove(location);
            }
        }

        @Override
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            super.didFailToLoadRewardedVideo(location, error);

            if (mLocationsToLoad.contains(location)) {
                MoPubLog.d("Chartboost rewarded video cache failed for location " + location + ".");
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(ChartboostRewardedVideo.class, location, VIDEO_DOWNLOAD_ERROR);
                mLocationsToLoad.remove(location);
            }
        }

        @Override
        public void didDismissRewardedVideo(String location) {
            // This is called before didCloseRewardedVideo and didClickRewardedVideo
            super.didDismissRewardedVideo(location);
            MoPubRewardedVideoManager.onRewardedVideoClosed(ChartboostRewardedVideo.class, location);
            MoPubLog.d("Chartboost rewarded video dismissed for location " + location + ".");
        }

        @Override
        public void didCloseRewardedVideo(String location) {
            super.didCloseRewardedVideo(location);
            MoPubLog.d("Chartboost rewarded video closed for location " + location + ".");
        }

        @Override
        public void didClickRewardedVideo(String location) {
            super.didClickRewardedVideo(location);
            MoPubRewardedVideoManager.onRewardedVideoClicked(ChartboostRewardedVideo.class, location);
            MoPubLog.d("Chartboost rewarded video clicked for location " + location + ".");
        }

        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            super.didCompleteRewardedVideo(location, reward);
            MoPubLog.d("Chartboost rewarded video completed for location " + location + " with "
                    + "reward amount " + reward);
            MoPubRewardedVideoManager.onRewardedVideoCompleted(
                    ChartboostRewardedVideo.class,
                    location,
                    MoPubReward.success(MoPubReward.NO_REWARD_LABEL, reward));
        }

        @Override
        public void didDisplayRewardedVideo(String location) {
            super.didDisplayRewardedVideo(location);
            MoPubLog.d("Chartboost rewarded video displayed for location " + location + ".");
            MoPubRewardedVideoManager.onRewardedVideoStarted(ChartboostRewardedVideo.class, location);
        }
    }

    private static final class ChartboostLifecycleListener implements LifecycleListener {
        @Override
        public void onCreate(@NonNull Activity activity) {
            Chartboost.onCreate(activity);
        }

        @Override
        public void onStart(@NonNull Activity activity) {
            Chartboost.onStart(activity);
        }

        @Override
        public void onPause(@NonNull Activity activity) {
            Chartboost.onPause(activity);
        }

        @Override
        public void onResume(@NonNull Activity activity) {
            Chartboost.onResume(activity);
        }

        @Override
        public void onRestart(@NonNull Activity activity) {
        }

        @Override
        public void onStop(@NonNull Activity activity) {
            Chartboost.onStop(activity);
        }

        @Override
        public void onDestroy(@NonNull Activity activity) {
            Chartboost.onDestroy(activity);
        }

        @Override
        public void onBackPressed(@NonNull Activity activity) {
            Chartboost.onBackPressed();
        }
    }

    public static final class ChartboostMediationSettings implements MediationSettings {
        @NonNull private final String mCustomId;

        public ChartboostMediationSettings(@NonNull final String customId) {
            mCustomId = customId;
        }

        @NonNull public String getCustomId() {
            return mCustomId;
        }
    }

    @Deprecated // for testing
    @VisibleForTesting
    static void resetInitialization() {
        sInitialized = false;
    }
}
