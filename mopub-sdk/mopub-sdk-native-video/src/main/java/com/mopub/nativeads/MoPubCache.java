// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.mopub.common.Preconditions;
import com.mopub.common.VisibleForTesting;
import com.mopub.common.util.DeviceUtils;

import java.io.File;

class MoPubCache {

    private static final String NATIVE_CACHE_NAME = "mopub-native-cache";

    private volatile static Cache sInstance = null;

    @Nullable
    static Cache getCacheInstance(@NonNull final Context context) {
        Preconditions.checkNotNull(context);

        Cache instance = sInstance;
        if (instance == null) {
            synchronized (MoPubCache.class) {
                instance = sInstance;
                if (instance == null) {
                    final File cacheDir = context.getApplicationContext().getCacheDir();

                    if (cacheDir != null) {
                        final File nativeCacheDir = new File(cacheDir.getPath()
                                + File.separator
                                + NATIVE_CACHE_NAME);
                        final long cacheSize = DeviceUtils.diskCacheSizeBytes(cacheDir);
                        final LeastRecentlyUsedCacheEvictor lruEvictor = new LeastRecentlyUsedCacheEvictor(cacheSize);
                        instance = new SimpleCache(nativeCacheDir, lruEvictor);
                        sInstance = instance;
                    }
                }
            }
        }

        return instance;
    }

    @VisibleForTesting
    static void resetInstance() {
        if (sInstance != null) {
            sInstance.release();
            sInstance = null;
        }
    }
}
