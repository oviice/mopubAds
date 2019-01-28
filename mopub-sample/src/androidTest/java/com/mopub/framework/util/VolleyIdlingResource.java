// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.framework.util;

import android.content.Context;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import com.mopub.network.Networking;
import com.mopub.volley.Request;
import com.mopub.volley.RequestQueue;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * If writing an Espresso test that makes network calls, this resource must be registered in order
 * to ensure that Espresso waits for the Volley calls to resolve.
 */
public class VolleyIdlingResource implements IdlingResource {
    private static final String TAG = "VolleyIdlingResource";
    private final String mResourceName;

    // written from main thread, read from any thread.
    private volatile ResourceCallback mResourceCallback;

    private Field mCurrentRequests;
    private RequestQueue mVolleyRequestQueue;

    public VolleyIdlingResource(String resourceName, Context context) throws SecurityException, NoSuchFieldException {
        mResourceName = resourceName;
        mVolleyRequestQueue = Networking.getRequestQueue(context);
        mCurrentRequests = RequestQueue.class.getDeclaredField("mCurrentRequests");
        mCurrentRequests.setAccessible(true);
    }

    @Override
    public String getName() {
        return mResourceName;
    }

    @Override
    public boolean isIdleNow() {
        try {
            Set<Request> set = (Set<Request>) mCurrentRequests.get(mVolleyRequestQueue);
            if (set.isEmpty()) {
                Log.d(TAG, "Volley is idle.");
                mResourceCallback.onTransitionToIdle();
            } else {
                Log.d(TAG, "Volley is not idle.");
            }
            return set.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.mResourceCallback = resourceCallback;
    }
}

