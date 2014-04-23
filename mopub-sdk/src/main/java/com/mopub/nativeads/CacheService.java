package com.mopub.nativeads;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import com.mopub.common.util.DeviceUtils;
import com.mopub.common.util.Streams;
import com.mopub.common.util.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.mopub.nativeads.DiskLruCache.open;
import static com.mopub.nativeads.util.Utils.MoPubLog;

class CacheService {
    static final String UNIQUE_CACHE_NAME = "mopub-cache";
    private static final int APP_VERSION = 1;
    // The number of values per cache entry. Must be positive.
    private static final int VALUE_COUNT = 1;

    private static DiskLruCache sDiskLruCache;
    private static MemoryLruCache sMemoryLruCache;

    static void initializeCaches(final Context context) {
        if (sDiskLruCache == null) {
            final File cacheDirectory = getDiskCacheDirectory(context);
            final long diskCacheSizeBytes = DeviceUtils.diskCacheSizeBytes(cacheDirectory);
            try {
                sDiskLruCache = open(
                        cacheDirectory,
                        APP_VERSION,
                        VALUE_COUNT,
                        diskCacheSizeBytes
                );
            } catch (IOException e) {
                MoPubLog("Unable to create DiskLruCache", e);
            }
        }

        if (sMemoryLruCache == null) {
            final int memoryCacheSizeBytes = DeviceUtils.memoryCacheSizeBytes(context);
            sMemoryLruCache = new MemoryLruCache(memoryCacheSizeBytes);
        }
    }

    static String createValidDiskCacheKey(final String key) {
        return Utils.sha1(key);
    }

    static File getDiskCacheDirectory(final Context context) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + UNIQUE_CACHE_NAME);
    }

    static byte[] getFromMemoryCache(final String key) {
        if (sMemoryLruCache == null) {
            return null;
        }

        return sMemoryLruCache.get(key);
    }

    static byte[] getFromDiskCache(final String key) {
        if (sDiskLruCache == null) {
            return null;
        }

        byte[] bytes = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = sDiskLruCache.get(createValidDiskCacheKey(key));
            if (snapshot == null) {
                return null;
            }

            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                bytes = new byte[(int)snapshot.getLength(0)];
                final BufferedInputStream buffIn = new BufferedInputStream(in);
                try {
                    Streams.readStream(buffIn, bytes);
                } finally {
                    Streams.closeStream(buffIn);
                }
            }
        } catch (Exception e) {
            MoPubLog("Unable to get from DiskLruCache", e);
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return bytes;
    }

    static void getFromDiskCacheAsync(final String key, final DiskLruCacheGetListener diskLruCacheGetListener) {
        new DiskLruCacheGetTask(key, diskLruCacheGetListener).execute();
    }

    static byte[] get(final String key) {
        byte[] bytes = getFromMemoryCache(key);
        if (bytes != null) {
            return bytes;
        }
        return getFromDiskCache(key);
    }

    static void putToMemoryCache(final String key, final byte[] content) {
        if (sMemoryLruCache == null) {
            return;
        }

        sMemoryLruCache.put(key, content);
    }

    static void putToDiskCache(final String key, final byte[] content) {
        if (sDiskLruCache == null) {
            return;
        }

        DiskLruCache.Editor editor = null;
        try {
            editor = sDiskLruCache.edit(createValidDiskCacheKey(key));

            if (editor == null) {
                // another edit is in progress
                return;
            }

            final OutputStream outputStream = editor.newOutputStream(0);
            outputStream.write(content);
            outputStream.close();

            sDiskLruCache.flush();
            editor.commit();
        } catch (Exception e) {
            MoPubLog("Unable to put to DiskLruCache", e);
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    static void putToDiskCacheAsync(final String key, final byte[] content) {
        new DiskLruCachePutTask(key, content).execute();
    }

    static void put(final String key, final byte[] content) {
        putToMemoryCache(key, content);
        putToDiskCacheAsync(key, content);
    }

    private static class MemoryLruCache extends LruCache<String, byte[]> {
        public MemoryLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(final String key, final byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                return bytes.length;
            }

            return super.sizeOf(key, bytes);
        }
    }

    static interface DiskLruCacheGetListener {
        void onComplete(final String key, final byte[] content);
    }

    private static class DiskLruCacheGetTask extends AsyncTask<Void, Void, byte[]> {
        private final DiskLruCacheGetListener mDiskLruCacheGetListener;
        private final String mKey;

        DiskLruCacheGetTask(final String key, final DiskLruCacheGetListener diskLruCacheGetListener) {
            mDiskLruCacheGetListener = diskLruCacheGetListener;
            mKey = key;
        }

        @Override
        protected byte[] doInBackground(Void... voids) {
            return CacheService.getFromDiskCache(mKey);
        }

        @Override
        protected void onPostExecute(final byte[] bytes) {
            if (isCancelled()) {
                onCancelled();
                return;
            }

            if (mDiskLruCacheGetListener != null) {
                mDiskLruCacheGetListener.onComplete(mKey, bytes);
            }
        }

        @Override
        protected void onCancelled() {
            if (mDiskLruCacheGetListener != null) {
                mDiskLruCacheGetListener.onComplete(mKey, null);
            }
        }
    }

    private static class DiskLruCachePutTask extends AsyncTask<Void, Void, Void> {
        private final String mKey;
        private final byte[] mContent;

        DiskLruCachePutTask(final String key, final byte[] content) {
            mKey = key;
            mContent = content;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            CacheService.putToDiskCache(mKey, mContent);
            return null;
        }
    }

    // Testing
    @Deprecated
    static void clearAndNullCaches() {
        if (sDiskLruCache != null) {
            try {
                sDiskLruCache.delete();
                sDiskLruCache = null;
            } catch (IOException ignore) {
                sDiskLruCache = null;
            }
        }
        if (sMemoryLruCache != null) {
            sMemoryLruCache.evictAll();
            sMemoryLruCache = null;
        }
    }

    // Testing
    @Deprecated
    static LruCache<String, byte[]> getMemoryLruCache() {
        return sMemoryLruCache;
    }

    // Testing
    @Deprecated
    static DiskLruCache getDiskLruCache() {
        return sDiskLruCache;
    }
}
