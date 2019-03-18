// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.nativeads;

import android.content.Context;

import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoPubCacheTest {
    private static final String BASE_CACHE_DIR = "/some_cache_dir";

    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
    }

    @Test
    public void getCacheInstance_whenCacheDirIsNull_shouldReturnNull() {
        when(mockContext.getCacheDir()).thenReturn(null);

        Cache cache = MoPubCache.getCacheInstance(mockContext);
        assertThat(cache).isNull();
    }

    @Test
    public void getCacheInstance_whenCacheDirIsNonNull_shouldReturnSimpleCacheInstance() {
        when(mockContext.getCacheDir()).thenReturn(new File(BASE_CACHE_DIR));

        Cache cache = MoPubCache.getCacheInstance(mockContext);
        assertThat(cache).isInstanceOf(SimpleCache.class);
    }

    @After
    public void tearDown() {
        MoPubCache.resetInstance();
    }
}
