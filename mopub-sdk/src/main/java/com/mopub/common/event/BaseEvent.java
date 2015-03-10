package com.mopub.common.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.ClientMetadata;
import com.mopub.common.VisibleForTesting;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.mopub.common.ClientMetadata.MoPubNetworkType;

public abstract class BaseEvent {

    public static enum ScribeCategory {
        EXCHANGE_CLIENT_EVENT("exchange_client_event"),
        EXCHANGE_CLIENT_ERROR("exchange_client_error");

        private final String mScribeCategory;
        ScribeCategory(String scribeCategory) {
            mScribeCategory = scribeCategory;
        }

        public String getCategory() {
            return mScribeCategory;
        }
    }

    public static enum SdkProduct {
        NONE(0),
        WEB_VIEW(1),
        NATIVE(2);

        private final int mType;
        SdkProduct(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }
    }

    public static enum AppPlatform {
        IOS(0),
        ANDROID(1),
        MOBILE_WEB(2);

        private final int mType;
        AppPlatform(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }
    }

    @Nullable private ScribeCategory mScribeCategory;
    @Nullable private final String mEventName;
    @Nullable private final String mEventCategory;
    @Nullable private final SdkProduct mSdkProduct;
    @Nullable private final String mAdUnitId;
    @Nullable private final String mAdCreativeId;
    @Nullable private final String mAdType;
    @Nullable private final String mAdNetworkType;
    @Nullable private final Double mAdWidthPx;
    @Nullable private final Double mAdHeightPx;
    @Nullable private final Double mGeoLat;
    @Nullable private final Double mGeoLon;
    @Nullable private final Double mGeoAccuracy;
    @Nullable private final Double mPerformanceDurationMs;
    @Nullable private final String mRequestId;
    @Nullable private final Integer mRequestStatusCode;
    @Nullable private final String mRequestUri;
    @Nullable private final Integer mRequestRetries;
    @Nullable private final Long mTimestampUtcMs;
    @Nullable private ClientMetadata mClientMetaData;

    public BaseEvent(@NonNull final Builder builder) {
        mScribeCategory = builder.mScribeCategory;
        mEventName = builder.mEventName;
        mEventCategory = builder.mEventCategory;
        mSdkProduct = builder.mSdkProduct;
        mAdUnitId = builder.mAdUnitId;
        mAdCreativeId = builder.mAdCreativeId;
        mAdType = builder.mAdType;
        mAdNetworkType = builder.mAdNetworkType;
        mAdWidthPx = builder.mAdWidthPx;
        mAdHeightPx = builder.mAdHeightPx;
        mGeoLat = builder.mGeoLat;
        mGeoLon = builder.mGeoLon;
        mGeoAccuracy = builder.mGeoAccuracy;
        mPerformanceDurationMs = builder.mPerformanceDurationMs;
        mRequestId = builder.mRequestId;
        mRequestStatusCode = builder.mRequestStatusCode;
        mRequestUri = builder.mRequestUri;
        mRequestRetries = builder.mRequestRetries;
        mTimestampUtcMs = System.currentTimeMillis();
        mClientMetaData = ClientMetadata.getInstance();
    }

    public ScribeCategory getScribeCategory() {
        return mScribeCategory;
    }

    public String getEventName() {
        return mEventName;
    }

    public String getEventCategory() {
        return mEventCategory;
    }

    public SdkProduct getSdkProduct() {
        return mSdkProduct;
    }

    public String getSdkVersion() {
        return mClientMetaData == null ? null : mClientMetaData.getSdkVersion();
    }

    public String getAdUnitId() {
        return mAdUnitId;
    }

    public String getAdCreativeId() {
        return mAdCreativeId;
    }

    public String getAdType() {
        return mAdType;
    }

    public String getAdNetworkType() {
        return mAdNetworkType;
    }

    public Double getAdWidthPx() {
        return mAdWidthPx;
    }

    public Double getAdHeightPx() {
        return mAdHeightPx;
    }

    public AppPlatform getAppPlatform() {
        return AppPlatform.ANDROID;
    }

    public String getAppName() {
        return mClientMetaData == null ? null : mClientMetaData.getAppName();
    }

    public String getAppPackageName() {
        return mClientMetaData == null ? null : mClientMetaData.getAppPackageName();
    }

    public String getAppVersion() {
        return mClientMetaData == null ? null : mClientMetaData.getAppVersion();
    }

    public String getClientAdvertisingId() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceId();
    }

    public Boolean getClientDoNotTrack() {
        return mClientMetaData == null ? null : mClientMetaData.isDoNotTrackSet();
    }

    public String getDeviceManufacturer() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceManufacturer();
    }

    public String getDeviceModel() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceModel();
    }

    public String getDeviceProduct() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceProduct();
    }

    public String getDeviceOsVersion() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceOsVersion();
    }

    public Integer getDeviceScreenWidthPx() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceScreenWidthPx();
    }

    public Integer getDeviceScreenHeightPx() {
        return mClientMetaData == null ? null : mClientMetaData.getDeviceScreenHeightPx();
    }

    public Double getGeoLat() {
        return mGeoLat;
    }

    public Double getGeoLon() {
        return mGeoLon;
    }

    public Double getGeoAccuracy() {
        return mGeoAccuracy;
    }

    public Double getPerformanceDurationMs() {
        return mPerformanceDurationMs;
    }

    public MoPubNetworkType getNetworkType() {
        return mClientMetaData == null ? null : mClientMetaData.getActiveNetworkType();
    }

    public String getNetworkOperatorCode() {
        return mClientMetaData == null ? null : mClientMetaData.getNetworkOperator();
    }

    public String getNetworkOperatorName() {
        return mClientMetaData == null ? null : mClientMetaData.getNetworkOperatorName();
    }

    public String getNetworkIsoCountryCode() {
        return mClientMetaData == null ? null : mClientMetaData.getIsoCountryCode();
    }

    public String getNetworkSimCode() {
        return mClientMetaData == null ? null : mClientMetaData.getSimOperator();
    }

    public String getNetworkSimOperatorName() {
        return mClientMetaData == null ? null : mClientMetaData.getSimOperatorName();
    }

    public String getNetworkSimIsoCountryCode() {
        return mClientMetaData == null ? null : mClientMetaData.getSimIsoCountryCode();
    }

    public String getRequestId() {
        return mRequestId;
    }

    public Integer getRequestStatusCode() {
        return mRequestStatusCode;
    }

    public String getRequestUri() {
        return mRequestUri;
    }

    public Integer getRequestRetries() {
        return mRequestRetries;
    }

    public Long getTimestampUtcMs() {
        return mTimestampUtcMs;
    }

    @Override
    public String toString() {
        return  "BaseEvent\n" +
                "ScribeCategory: " + getScribeCategory() + "\n" +
                "EventName: " + getEventName() + "\n" +
                "EventCategory: " + getEventCategory() + "\n" +
                "SdkProduct: " + getSdkProduct() + "\n" +
                "SdkVersion: " + getSdkVersion() + "\n" +
                "AdUnitId: " + getAdUnitId() + "\n" +
                "AdCreativeId: " + getAdCreativeId() + "\n" +
                "AdType: " + getAdType() + "\n" +
                "AdNetworkType: " + getAdNetworkType() + "\n" +
                "AdWidthPx: " + getAdWidthPx() + "\n" +
                "AdHeightPx: " + getAdHeightPx() + "\n" +
                "AppPlatform: " + getAppPlatform() + "\n" +
                "AppName: " + getAppName() + "\n" +
                "AppPackageName: " + getAppPackageName() + "\n" +
                "AppVersion: " + getAppVersion() + "\n" +
                "DeviceManufacturer: " + getDeviceManufacturer() + "\n" +
                "DeviceModel: " + getDeviceModel() + "\n" +
                "DeviceProduct: " + getDeviceProduct() + "\n" +
                "DeviceOsVersion: " + getDeviceOsVersion() + "\n" +
                "DeviceScreenWidth: " + getDeviceScreenWidthPx() + "\n" +
                "DeviceScreenHeight: " + getDeviceScreenHeightPx() + "\n" +
                "GeoLat: " + getGeoLat() + "\n" +
                "GeoLon: " + getGeoLon() + "\n" +
                "GeoAccuracy: " + getGeoAccuracy() + "\n" +
                "PerformanceDurationMs: " + getPerformanceDurationMs() + "\n" +
                "NetworkType: " + getNetworkType() + "\n" +
                "NetworkOperatorCode: " + getNetworkOperatorCode() + "\n" +
                "NetworkOperatorName: " + getNetworkOperatorName() + "\n" +
                "NetworkIsoCountryCode: " + getNetworkIsoCountryCode() + "\n" +
                "NetworkSimCode: " + getNetworkSimCode() + "\n" +
                "NetworkSimOperatorName: " + getNetworkSimOperatorName() + "\n" +
                "NetworkSimIsoCountryCode: " + getNetworkSimIsoCountryCode() + "\n" +
                "RequestId: " + getRequestId() + "\n" +
                "RequestStatusCode: " + getRequestStatusCode() + "\n" +
                "RequestUri: " + getRequestUri() + "\n" +
                "RequestRetries" + getRequestRetries() + "\n" +
                "TimestampUtcMs: " + new SimpleDateFormat().format(new Date(getTimestampUtcMs())) + "\n";
    }

    @VisibleForTesting
    void setClientMetaData(ClientMetadata clientMetaData) {
        mClientMetaData = clientMetaData;
    }

    static abstract class Builder {
        @Nullable private ScribeCategory mScribeCategory;
        @Nullable private String mEventName;
        @Nullable private String mEventCategory;
        @Nullable private SdkProduct mSdkProduct;
        @Nullable private String mAdUnitId;
        @Nullable private String mAdCreativeId;
        @Nullable private String mAdType;
        @Nullable private String mAdNetworkType;
        @Nullable private Double mAdWidthPx;
        @Nullable private Double mAdHeightPx;
        @Nullable private Double mGeoLat;
        @Nullable private Double mGeoLon;
        @Nullable private Double mGeoAccuracy;
        @Nullable private Double mPerformanceDurationMs;
        @Nullable private String mRequestId;
        @Nullable private Integer mRequestStatusCode;
        @Nullable private String mRequestUri;
        @Nullable private Integer mRequestRetries;

        public Builder(ScribeCategory scribeCategory, String eventName, String eventCategory) {
            mScribeCategory = scribeCategory;
            mEventName = eventName;
            mEventCategory = eventCategory;
        }

        public Builder withSdkProduct(SdkProduct sdkProduct) {
            mSdkProduct = sdkProduct;
            return this;
        }

        public Builder withAdUnitId(String adUnitId) {
            mAdUnitId = adUnitId;
            return this;
        }

        public Builder withAdCreativeId(String adCreativeId) {
            mAdCreativeId = adCreativeId;
            return this;
        }

        public Builder withAdType(String adType) {
            mAdType = adType;
            return this;
        }

        public Builder withAdNetworkType(String adNetworkType) {
            mAdNetworkType = adNetworkType;
            return this;
        }

        public Builder withAdWidthPx(Double adWidthPx) {
            mAdWidthPx = adWidthPx;
            return this;
        }

        public Builder withAdHeightPx(Double adHeightPx) {
            mAdHeightPx = adHeightPx;
            return this;
        }

        public Builder withGeoLat(Double geoLat) {
            mGeoLat = geoLat;
            return this;
        }

        public Builder withGeoLon(Double geoLon) {
            mGeoLon = geoLon;
            return this;
        }

        public Builder withGeoAccuracy(Double geoAccuracy) {
            mGeoAccuracy = geoAccuracy;
            return this;
        }

        public Builder withPerformanceDurationMs(Double performanceDurationMs) {
            mPerformanceDurationMs = performanceDurationMs;
            return this;
        }

        public Builder withRequestId(String requestId) {
            mRequestId = requestId;
            return this;
        }

        public Builder withRequestStatusCode(Integer requestStatusCode) {
            mRequestStatusCode = requestStatusCode;
            return this;
        }

        public Builder withRequestUri(String requestUri) {
            mRequestUri = requestUri;
            return this;
        }

        public Builder withRequestRetries(Integer requestRetries) {
            mRequestRetries = requestRetries;
            return this;
        }

        public abstract BaseEvent build();
    }
}
