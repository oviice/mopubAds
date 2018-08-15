package com.mopub.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.MoPub.BrowserAgent;
import com.mopub.common.Preconditions;
import com.mopub.common.util.DateAndTime;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final String mAdType;

    @Nullable
    private final String mAdUnitId;

    @Nullable
    private final String mFullAdType;
    @Nullable
    private final String mNetworkType;

    @Nullable
    private final String mRewardedVideoCurrencyName;
    @Nullable
    private final String mRewardedVideoCurrencyAmount;
    @Nullable
    private final String mRewardedCurrencies;
    @Nullable
    private final String mRewardedVideoCompletionUrl;
    @Nullable
    private final Integer mRewardedDuration;
    private final boolean mShouldRewardOnClick;

    @Nullable
    private final String mRedirectUrl;
    @Nullable
    private final String mClickTrackingUrl;
    @NonNull
    private final List<String> mImpressionTrackingUrls;
    @Nullable
    private final String mFailoverUrl;
    @Nullable
    private final String mBeforeLoadUrl;
    @Nullable
    private final String mAfterLoadUrl;
    @Nullable
    private final String mRequestId;

    @Nullable
    private final Integer mWidth;
    @Nullable
    private final Integer mHeight;
    @Nullable
    private final Integer mAdTimeoutDelayMillis;
    @Nullable
    private final Integer mRefreshTimeMillis;
    @Nullable
    private final String mDspCreativeId;

    private final boolean mScrollable;

    @Nullable
    private final String mResponseBody;
    @Nullable
    private final JSONObject mJsonBody;

    @Nullable
    private final String mCustomEventClassName;
    @Nullable
    private final BrowserAgent mBrowserAgent;
    @NonNull
    private final Map<String, String> mServerExtras;

    private final long mTimestamp;

    private AdResponse(@NonNull Builder builder) {

        mAdType = builder.adType;
        mAdUnitId = builder.adUnitId;
        mFullAdType = builder.fullAdType;
        mNetworkType = builder.networkType;

        mRewardedVideoCurrencyName = builder.rewardedVideoCurrencyName;
        mRewardedVideoCurrencyAmount = builder.rewardedVideoCurrencyAmount;
        mRewardedCurrencies = builder.rewardedCurrencies;
        mRewardedVideoCompletionUrl = builder.rewardedVideoCompletionUrl;
        mRewardedDuration = builder.rewardedDuration;
        mShouldRewardOnClick = builder.shouldRewardOnClick;

        mRedirectUrl = builder.redirectUrl;
        mClickTrackingUrl = builder.clickTrackingUrl;
        mImpressionTrackingUrls = builder.impressionTrackingUrls;
        mFailoverUrl = builder.failoverUrl;
        mBeforeLoadUrl = builder.beforeLoadUrl;
        mAfterLoadUrl = builder.afterLoadUrl;
        mRequestId = builder.requestId;
        mWidth = builder.width;
        mHeight = builder.height;
        mAdTimeoutDelayMillis = builder.adTimeoutDelayMillis;
        mRefreshTimeMillis = builder.refreshTimeMillis;
        mDspCreativeId = builder.dspCreativeId;
        mScrollable = builder.scrollable;
        mResponseBody = builder.responseBody;
        mJsonBody = builder.jsonBody;
        mCustomEventClassName = builder.customEventClassName;
        mBrowserAgent = builder.browserAgent;
        mServerExtras = builder.serverExtras;
        mTimestamp = DateAndTime.now().getTime();
    }

    public boolean hasJson() {
        return mJsonBody != null;
    }

    @Nullable
    public JSONObject getJsonBody() {
        return mJsonBody;
    }

    @Nullable
    public String getStringBody() {
        return mResponseBody;
    }

    @Nullable
    public String getAdType() {
        return mAdType;
    }

    @Nullable
    public String getFullAdType() {
        return mFullAdType;
    }

    @Nullable
    public String getAdUnitId() {
        return mAdUnitId;
    }

    @Nullable
    public String getNetworkType() {
        return mNetworkType;
    }

    @Nullable
    public String getRewardedVideoCurrencyName() {
        return mRewardedVideoCurrencyName;
    }

    @Nullable
    public String getRewardedVideoCurrencyAmount() {
        return mRewardedVideoCurrencyAmount;
    }

    @Nullable
    public String getRewardedCurrencies() {
        return mRewardedCurrencies;
    }

    @Nullable
    public String getRewardedVideoCompletionUrl() {
        return mRewardedVideoCompletionUrl;
    }

    @Nullable
    public Integer getRewardedDuration() {
        return mRewardedDuration;
    }

    public boolean shouldRewardOnClick() {
        return mShouldRewardOnClick;
    }

    @Nullable
    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    @Nullable
    public String getClickTrackingUrl() {
        return mClickTrackingUrl;
    }

    @NonNull
    public List<String> getImpressionTrackingUrls() {
        return mImpressionTrackingUrls;
    }

    @Deprecated
    @Nullable
    public String getFailoverUrl() {
        return mFailoverUrl;
    }

    @Nullable
    public String getBeforeLoadUrl() {
        return mBeforeLoadUrl;
    }

    @Nullable
    public String getAfterLoadUrl() {
        return mAfterLoadUrl;
    }

    @Nullable
    public String getRequestId() {
        return mRequestId;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Nullable
    public Integer getWidth() {
        return mWidth;
    }

    @Nullable
    public Integer getHeight() {
        return mHeight;
    }

    @NonNull
    public Integer getAdTimeoutMillis(int defaultValue) {
        if (mAdTimeoutDelayMillis == null || mAdTimeoutDelayMillis < 1000) {
            return defaultValue;
        }
        return mAdTimeoutDelayMillis;
    }

    @Nullable
    public Integer getRefreshTimeMillis() {
        return mRefreshTimeMillis;
    }

    @Nullable
    public String getDspCreativeId() {
        return mDspCreativeId;
    }

    @Nullable
    public String getCustomEventClassName() {
        return mCustomEventClassName;
    }

    @Nullable
    public BrowserAgent getBrowserAgent() { return mBrowserAgent; }

    @NonNull
    public Map<String, String> getServerExtras() {
        // Strings are immutable, so this works as a "deep" copy.
        return new TreeMap<>(mServerExtras);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public Builder toBuilder() {
        return new Builder()
                .setAdType(mAdType)
                .setNetworkType(mNetworkType)
                .setRewardedVideoCurrencyName(mRewardedVideoCurrencyName)
                .setRewardedVideoCurrencyAmount(mRewardedVideoCurrencyAmount)
                .setRewardedCurrencies(mRewardedCurrencies)
                .setRewardedVideoCompletionUrl(mRewardedVideoCompletionUrl)
                .setRewardedDuration(mRewardedDuration)
                .setShouldRewardOnClick(mShouldRewardOnClick)
                .setRedirectUrl(mRedirectUrl)
                .setClickTrackingUrl(mClickTrackingUrl)
                .setImpressionTrackingUrls(mImpressionTrackingUrls)
                .setFailoverUrl(mFailoverUrl)
                .setBeforeLoadUrl(mBeforeLoadUrl)
                .setAfterLoadUrl(mAfterLoadUrl)
                .setDimensions(mWidth, mHeight)
                .setAdTimeoutDelayMilliseconds(mAdTimeoutDelayMillis)
                .setRefreshTimeMilliseconds(mRefreshTimeMillis)
                .setDspCreativeId(mDspCreativeId)
                .setScrollable(mScrollable)
                .setResponseBody(mResponseBody)
                .setJsonBody(mJsonBody)
                .setCustomEventClassName(mCustomEventClassName)
                .setBrowserAgent(mBrowserAgent)
                .setServerExtras(mServerExtras);
    }

    public static class Builder {
        private String adType;
        private String adUnitId;
        private String fullAdType;
        private String networkType;

        private String rewardedVideoCurrencyName;
        private String rewardedVideoCurrencyAmount;
        private String rewardedCurrencies;
        private String rewardedVideoCompletionUrl;
        private Integer rewardedDuration;
        private boolean shouldRewardOnClick;

        private String redirectUrl;
        private String clickTrackingUrl;
        private List<String> impressionTrackingUrls = new ArrayList<>();
        private String failoverUrl;
        private String beforeLoadUrl;
        private String afterLoadUrl;
        private String requestId;

        private Integer width;
        private Integer height;
        private Integer adTimeoutDelayMillis;
        private Integer refreshTimeMillis;
        private String dspCreativeId;

        private boolean scrollable = false;

        private String responseBody;
        private JSONObject jsonBody;

        private String customEventClassName;
        private BrowserAgent browserAgent;

        private Map<String, String> serverExtras = new TreeMap<>();

        public Builder setAdType(@Nullable final String adType) {
            this.adType = adType;
            return this;
        }

        public Builder setAdUnitId(@Nullable final String adUnitId) {
            this.adUnitId = adUnitId;
            return this;
        }

        public Builder setFullAdType(@Nullable final String fullAdType) {
            this.fullAdType = fullAdType;
            return this;
        }

        public Builder setNetworkType(@Nullable final String networkType) {
            this.networkType = networkType;
            return this;
        }

        public Builder setRewardedVideoCurrencyName(
                @Nullable final String rewardedVideoCurrencyName) {
            this.rewardedVideoCurrencyName = rewardedVideoCurrencyName;
            return this;
        }

        public Builder setRewardedVideoCurrencyAmount(
                @Nullable final String rewardedVideoCurrencyAmount) {
            this.rewardedVideoCurrencyAmount = rewardedVideoCurrencyAmount;
            return this;
        }

        public Builder setRewardedCurrencies(@Nullable final String rewardedCurrencies) {
            this.rewardedCurrencies = rewardedCurrencies;
            return this;
        }

        public Builder setRewardedVideoCompletionUrl(
                @Nullable final String rewardedVideoCompletionUrl) {
            this.rewardedVideoCompletionUrl = rewardedVideoCompletionUrl;
            return this;
        }

        public Builder setRewardedDuration(@Nullable final Integer rewardedDuration) {
            this.rewardedDuration = rewardedDuration;
            return this;
        }

        public Builder setShouldRewardOnClick(final boolean shouldRewardOnClick) {
            this.shouldRewardOnClick = shouldRewardOnClick;
            return this;
        }

        public Builder setRedirectUrl(@Nullable final String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public Builder setClickTrackingUrl(@Nullable final String clickTrackingUrl) {
            this.clickTrackingUrl = clickTrackingUrl;
            return this;
        }

        public Builder setImpressionTrackingUrls(@NonNull final List<String> impressionTrackingUrls) {
            Preconditions.checkNotNull(impressionTrackingUrls);

            this.impressionTrackingUrls = impressionTrackingUrls;
            return this;
        }

        public Builder setFailoverUrl(@Nullable final String failoverUrl) {
            this.failoverUrl = failoverUrl;
            return this;
        }

        public Builder setBeforeLoadUrl(@Nullable final String beforeLoadUrl) {
            this.beforeLoadUrl = beforeLoadUrl;
            return this;
        }

        public Builder setAfterLoadUrl(@Nullable final String afterLoadUrl) {
            this.afterLoadUrl = afterLoadUrl;
            return this;
        }

        public Builder setRequestId(@Nullable final String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setDimensions(@Nullable final Integer width,
                @Nullable final Integer height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setAdTimeoutDelayMilliseconds(@Nullable final Integer adTimeoutDelayMilliseconds) {
            this.adTimeoutDelayMillis = adTimeoutDelayMilliseconds;
            return this;
        }

        public Builder setRefreshTimeMilliseconds(@Nullable final Integer refreshTimeMilliseconds) {
            this.refreshTimeMillis = refreshTimeMilliseconds;
            return this;
        }

        public Builder setScrollable(@Nullable final Boolean scrollable) {
            this.scrollable = scrollable == null ? this.scrollable : scrollable;
            return this;
        }

        public Builder setDspCreativeId(@Nullable final String dspCreativeId) {
            this.dspCreativeId = dspCreativeId;
            return this;
        }

        public Builder setResponseBody(@Nullable final String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder setJsonBody(@Nullable final JSONObject jsonBody) {
            this.jsonBody = jsonBody;
            return this;
        }

        public Builder setCustomEventClassName(@Nullable final String customEventClassName) {
            this.customEventClassName = customEventClassName;
            return this;
        }

        public Builder setBrowserAgent(@Nullable final BrowserAgent browserAgent) {
            this.browserAgent = browserAgent;
            return this;
        }

        public Builder setServerExtras(@Nullable final Map<String, String> serverExtras) {
            if (serverExtras == null) {
                this.serverExtras = new TreeMap<>();
            } else {
                this.serverExtras = new TreeMap<>(serverExtras);
            }
            return this;
        }

        public AdResponse build() {
            return new AdResponse(this);
        }
    }
}
