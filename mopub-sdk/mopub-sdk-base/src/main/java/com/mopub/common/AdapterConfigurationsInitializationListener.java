// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.support.annotation.NonNull;

import java.util.Map;

interface AdapterConfigurationsInitializationListener extends OnNetworkInitializationFinishedListener{
    void onAdapterConfigurationsInitialized(
            @NonNull final Map<String, AdapterConfiguration> adapterConfigurations);
}
