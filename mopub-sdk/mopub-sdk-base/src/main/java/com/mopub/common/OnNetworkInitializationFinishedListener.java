// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.support.annotation.NonNull;

import com.mopub.mobileads.MoPubErrorCode;

public interface OnNetworkInitializationFinishedListener {
    void onNetworkInitializationFinished(@NonNull final Class<? extends AdapterConfiguration> clazz,
            @NonNull final MoPubErrorCode moPubErrorCode);
}
