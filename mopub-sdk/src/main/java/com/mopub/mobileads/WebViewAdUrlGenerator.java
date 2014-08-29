package com.mopub.mobileads;

import android.content.Context;
import android.os.Build;

import com.mopub.common.AdUrlGenerator;
import com.mopub.common.GpsHelper;
import com.mopub.common.MoPub;

import static com.mopub.mobileads.util.Mraids.isStorePictureSupported;

public class WebViewAdUrlGenerator extends AdUrlGenerator {
    public WebViewAdUrlGenerator(Context context) {
        super(context);
    }

    @Override
    public String generateUrlString(String serverHostname) {
        initUrlString(serverHostname, MoPubView.AD_HANDLER);

        setApiVersion("6");

        setAdUnitId(mAdUnitId);

        setSdkVersion(MoPub.SDK_VERSION);

        setDeviceInfo(Build.MANUFACTURER, Build.MODEL, Build.PRODUCT);

        setUdid(getUdidFromContext(mContext));

        setDoNotTrack(GpsHelper.isLimitAdTrackingEnabled(mContext));

        setKeywords(mKeywords);

        setLocation(mLocation);

        setTimezone(AdUrlGenerator.getTimeZoneOffsetString());

        setOrientation(mContext.getResources().getConfiguration().orientation);

        setDensity(mContext.getResources().getDisplayMetrics().density);

        setMraidFlag(detectIsMraidSupported());

        String networkOperator = getNetworkOperator();
        setMccCode(networkOperator);
        setMncCode(networkOperator);

        setIsoCountryCode(mTelephonyManager.getNetworkCountryIso());
        setCarrierName(mTelephonyManager.getNetworkOperatorName());

        setNetworkType(getActiveNetworkType());

        setAppVersion(getAppVersionFromContext(mContext));

        setExternalStoragePermission(isStorePictureSupported(mContext));

        setTwitterAppInstalledFlag();

        return getFinalUrlString();
    }

    private boolean detectIsMraidSupported() {
        boolean mraid = true;
        try {
            Class.forName("com.mopub.mobileads.MraidView");
        } catch (ClassNotFoundException e) {
            mraid = false;
        }
        return mraid;
    }
}
