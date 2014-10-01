/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.common;

import android.content.Context;
import android.location.Location;

import com.mopub.common.util.IntentUtils;

import static com.mopub.common.ClientMetadata.MoPubNetworkType;

public abstract class AdUrlGenerator extends BaseUrlGenerator {
    private static TwitterAppInstalledStatus sTwitterAppInstalledStatus = TwitterAppInstalledStatus.UNKNOWN;

    protected Context mContext;
    protected String mAdUnitId;
    protected String mKeywords;
    protected Location mLocation;

    public static enum TwitterAppInstalledStatus {
        UNKNOWN,
        NOT_INSTALLED,
        INSTALLED,
    }

    public AdUrlGenerator(Context context) {
        mContext = context;
    }

    public AdUrlGenerator withAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
        return this;
    }

    public AdUrlGenerator withKeywords(String keywords) {
        mKeywords = keywords;
        return this;
    }

    public AdUrlGenerator withLocation(Location location) {
        mLocation = location;
        return this;
    }

    protected void setAdUnitId(String adUnitId) {
        addParam("id", adUnitId);
    }

    protected void setSdkVersion(String sdkVersion) {
        addParam("nv", sdkVersion);
    }

    protected void setKeywords(String keywords) {
        addParam("q", keywords);
    }

    protected void setLocation(Location location) {
        if (location != null) {
            addParam("ll", location.getLatitude() + "," + location.getLongitude());
            addParam("lla", "" + (int) location.getAccuracy());
        }
    }

    protected void setTimezone(String timeZoneOffsetString) {
        addParam("z", timeZoneOffsetString);
    }

    protected void setOrientation(String orientation) {
        addParam("o", orientation);
    }

    protected void setDensity(float density) {
        addParam("sc_a", "" + density);
    }

    protected void setMraidFlag(boolean mraid) {
        if (mraid) addParam("mr", "1");
    }

    protected void setMccCode(String networkOperator) {
        String mcc = networkOperator == null ? "" : networkOperator.substring(0, mncPortionLength(networkOperator));
        addParam("mcc", mcc);
    }

    protected void setMncCode(String networkOperator) {
        String mnc = networkOperator == null ? "" : networkOperator.substring(mncPortionLength(networkOperator));
        addParam("mnc", mnc);
    }

    protected void setIsoCountryCode(String networkCountryIso) {
        addParam("iso", networkCountryIso);
    }

    protected void setCarrierName(String networkOperatorName) {
        addParam("cn", networkOperatorName);
    }

    protected void setNetworkType(MoPubNetworkType networkType) {
        addParam("ct", networkType);
    }

    private void addParam(String key, MoPubNetworkType value) {
        addParam(key, value.toString());
    }

    private int mncPortionLength(String networkOperator) {
        return Math.min(3, networkOperator.length());
    }

    protected void setTwitterAppInstalledFlag() {
        if (sTwitterAppInstalledStatus == TwitterAppInstalledStatus.UNKNOWN) {
            sTwitterAppInstalledStatus = getTwitterAppInstallStatus();
        }

        if (sTwitterAppInstalledStatus == TwitterAppInstalledStatus.INSTALLED) {
            addParam("ts", "1");
        }
    }

    public TwitterAppInstalledStatus getTwitterAppInstallStatus() {
        return IntentUtils.canHandleTwitterUrl(mContext) ? TwitterAppInstalledStatus.INSTALLED : TwitterAppInstalledStatus.NOT_INSTALLED;
    }

    @Deprecated // for testing
    public static void setTwitterAppInstalledStatus(TwitterAppInstalledStatus status) {
        sTwitterAppInstalledStatus = status;
    }

    /**
     * @deprecated As of release 2.4
     */
    @Deprecated
    public AdUrlGenerator withFacebookSupported(boolean enabled) {
        return this;
    }
}
