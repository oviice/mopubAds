// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.mopub.common.logging.MoPubLog.SdkLogEvent.CUSTOM;

public abstract class BaseAdapterConfiguration implements AdapterConfiguration {

    @VisibleForTesting
    static final String CUSTOM_EVENT_PREF_NAME = "mopubCustomEventSettings";

    @Nullable private Map<String, String> mMoPubRequestOptions;

    @Override
    public void setCachedInitializationParameters(@NonNull final Context context,
            @Nullable final Map<String, String> initializationParameters) {
        Preconditions.checkNotNull(context);

        if (initializationParameters == null || initializationParameters.isEmpty()) {
            return;
        }

        SharedPreferences sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context,
                CUSTOM_EVENT_PREF_NAME);

        final String serverExtrasJsonString = (new JSONObject(initializationParameters)).toString();

        final String adapterConfigurationClassName = getClass().getName();

        MoPubLog.log(CUSTOM, String.format(Locale.US,
                "Updating init settings for custom event %s with params %s",
                adapterConfigurationClassName, serverExtrasJsonString));

        sharedPreferences
                .edit()
                .putString(adapterConfigurationClassName, serverExtrasJsonString)
                .apply();
    }

    @NonNull
    @Override
    public Map<String, String> getCachedInitializationParameters(@NonNull final Context context) {
        final SharedPreferences sharedPreferences = SharedPreferencesHelper.getSharedPreferences(
                context, CUSTOM_EVENT_PREF_NAME);
        final Map<String, ?> networkInitSettings = sharedPreferences.getAll();
        MoPubLog.log(CUSTOM, String.format(Locale.US, "fetched init settings for %s networks: %s",
                networkInitSettings.size(), networkInitSettings.keySet()));

        final String adapterConfigurationClassName = getClass().getName();
        final String networkInitParamsJsonString =
                (String) networkInitSettings.get(adapterConfigurationClassName);

        Map<String, String> networkInitParamsMap = new HashMap<>();
        try {
            networkInitParamsMap = Json.jsonStringToMap(networkInitParamsJsonString);
        } catch (JSONException e) {
            MoPubLog.log(CUSTOM, "Error fetching init settings for adapter configuration " +
                    adapterConfigurationClassName);
        }
        return networkInitParamsMap;
    }

    @Override
    public void setMoPubRequestOptions(@Nullable final Map<String, String> moPubRequestOptions) {
        mMoPubRequestOptions = moPubRequestOptions;
    }

    @Nullable
    @Override
    public Map<String, String> getMoPubRequestOptions() {
        return mMoPubRequestOptions;
    }
}
