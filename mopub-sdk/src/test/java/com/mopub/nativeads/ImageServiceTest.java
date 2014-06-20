package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mopub.common.CacheService;
import com.mopub.common.CacheServiceTest;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.mopub.nativeads.ImageService.ImageServiceListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class ImageServiceTest {

    private ImageServiceListener imageServiceListener;
    private Semaphore semaphore;
    private Map<String, Bitmap> bitmaps;
    private FakeHttpLayer fakeHttpLayer;
    private String url1;
    private String url2;
    private String url3;
    private String imageData1;
    private String imageData2;
    private String imageData3;
    private Context context;

    @Before
    public void setUp() throws Exception {
        semaphore = new Semaphore(0);
        imageServiceListener = mock(ImageServiceListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Map<String, Bitmap> bitmaps = (Map)args[0];
                ImageServiceTest.this.bitmaps = bitmaps;
                semaphore.release();
                return null;
            }
        }).when(imageServiceListener).onSuccess(anyMap());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                semaphore.release();
                return null;
            }
        }).when(imageServiceListener).onFail();

        fakeHttpLayer = Robolectric.getFakeHttpLayer();
        url1 = "http://www.mopub.com/";
        url2 = "http://www.twitter.com";
        url3 = "http://www.guydot.com";
        imageData1 = "image_data_1";
        imageData2 = "image_data_2";
        imageData3 = "image_data_3";
        context = new Activity();
    }

    @After
    public void tearDown() throws Exception {
        CacheService.clearAndNullCaches();
    }

    @Test
    public void get_shouldInitializeCaches() throws Exception {
        CacheService.clearAndNullCaches();
        assertThat(CacheService.getMemoryLruCache()).isNull();
        assertThat(CacheService.getDiskLruCache()).isNull();

        ImageService.get(context, new ArrayList<String>(), imageServiceListener);

        assertThat(CacheService.getMemoryLruCache()).isNotNull();
        assertThat(CacheService.getDiskLruCache()).isNotNull();
    }

    @Test
    public void get_withImageInMemoryCache_shouldReturnImage() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();
        CacheService.putToMemoryCache(url1, imageData1.getBytes());

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        // no need for semaphore since memory cache is synchronous

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
    }

    @Test
    public void get_withImageInDiskCache_shouldReturnImage() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();
        CacheService.putToDiskCache(url1, imageData1.getBytes());

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        semaphore.acquire();

        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
    }

    @Test
    public void get_withEmptyCaches_shouldGetImageFromNetwork() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
    }

    @Test
    public void get_withImagesInMemoryCacheAndDiskCache_shouldReturnBothImages() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData1.getBytes());
        CacheService.putToDiskCache(url2, imageData2.getBytes());

        ImageService.get(context, Arrays.asList(url1, url2), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo()).isNull();
        assertThat(shadowOf(bitmaps.get(url2)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withImagesInMemoryAndNetwork_shouldReturnBothImages() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData1.getBytes());
        fakeHttpLayer.addPendingHttpResponse(200, imageData2);

        ImageService.get(context, Arrays.asList(url1, url2), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo().getHttpHost().toString()).isEqualTo(url2);
        assertThat(shadowOf(bitmaps.get(url2)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withImagesInDiskAndNetwork_shouldReturnBothImages() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToDiskCache(url1, imageData1.getBytes());
        fakeHttpLayer.addPendingHttpResponse(200, imageData2);

        ImageService.get(context, Arrays.asList(url1, url2), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
        assertThat(fakeHttpLayer.getLastSentHttpRequestInfo().getHttpHost().toString()).isEqualTo(url2);
        assertThat(shadowOf(bitmaps.get(url2)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withImagesInMemoryAndDiskAndNetwork_shouldReturnAllImages() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData1.getBytes());
        CacheService.putToDiskCache(url2, imageData2.getBytes());
        fakeHttpLayer.addPendingHttpResponse(200, imageData3);

        ImageService.get(context, Arrays.asList(url1, url2, url3), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());
        assertThat(shadowOf(bitmaps.get(url2)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
        assertThat(shadowOf(bitmaps.get(url3)).getCreatedFromBytes()).isEqualTo(imageData3.getBytes());
    }

    @Test
    public void get_withSameKeysInMemoryAndDiskCache_shouldReturnValueFromMemoryCache() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData2.getBytes());
        CacheService.putToDiskCache(url1, imageData1.getBytes());

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withSameKeysInMemoryAndNetwork_shouldReturnValueFromMemoryCache() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData2.getBytes());
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withSameKeysInDiskAndNetwork_shouldReturnValueFromDiskCache() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToDiskCache(url1, imageData2.getBytes());
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        ImageService.get(context, Arrays.asList(url1), imageServiceListener);
        semaphore.acquire();

        assertThat(shadowOf(bitmaps.get(url1)).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());
    }

    @Test
    public void get_withNetworkFailure_shouldFail() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        CacheService.putToMemoryCache(url1, imageData1.getBytes());
        CacheService.putToDiskCache(url2, imageData2.getBytes());
        fakeHttpLayer.addPendingHttpResponse(500, imageData3);

        ImageService.get(context, Arrays.asList(url1, url2, url3), imageServiceListener);
        semaphore.acquire();

        assertThat(bitmaps).isNull();
    }

    @Test
    public void get_withMultipleNetworkSuccessAndOneFailure_shouldFail() throws Exception {
        CacheService.initializeCaches(context);
        CacheServiceTest.assertCachesAreEmpty();

        fakeHttpLayer.addPendingHttpResponse(200, imageData1);
        fakeHttpLayer.addPendingHttpResponse(200, imageData2);
        fakeHttpLayer.addPendingHttpResponse(500, imageData3);

        ImageService.get(context, Arrays.asList(url1, url2, url3), imageServiceListener);
        semaphore.acquire();

        assertThat(bitmaps).isNull();
    }

    @Test
    public void putBitmapsInCache_populatesCaches() throws Exception {
        CacheService.initializeCaches(context);

        Bitmap bitmap1 = BitmapFactory.decodeStream(getInputStreamFromString(imageData1));
        Bitmap bitmap2 = BitmapFactory.decodeStream(getInputStreamFromString(imageData2));

        Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>(2);
        bitmaps.put(url1, bitmap1);
        bitmaps.put(url2, bitmap2);

        assertThat(ImageService.getBitmapFromDiskCache(url1)).isNull();
        assertThat(ImageService.getBitmapFromDiskCache(url2)).isNull();
        assertThat(ImageService.getBitmapFromMemoryCache(url1)).isNull();
        assertThat(ImageService.getBitmapFromMemoryCache(url2)).isNull();

        ImageService.putBitmapsInCache(bitmaps);
        Thread.sleep(500); // disk cache put is async

        assertThat(ImageService.getBitmapFromDiskCache(url1)).isNotNull();
        assertThat(ImageService.getBitmapFromDiskCache(url2)).isNotNull();
        assertThat(ImageService.getBitmapFromMemoryCache(url1)).isNotNull();
        assertThat(ImageService.getBitmapFromMemoryCache(url2)).isNotNull();
    }

    @Test
    public void getBitmapsFromMemoryCache_withEmptyCacheAndTwoUrls_returnsNoCacheHitsAndTwoCacheMisses() throws Exception {
        CacheService.initializeCaches(context);
        assertThat(CacheService.getMemoryLruCache().size()).isEqualTo(0);

        Map<String, Bitmap> cacheHits = new HashMap<String, Bitmap>(2);
        List<String> cacheMisses =
                ImageService.getBitmapsFromMemoryCache(Arrays.asList(url1, url2), cacheHits);

        assertThat(cacheHits).isEmpty();
        assertThat(cacheMisses).containsOnly(url1, url2);
    }

    @Test
    public void getBitmapsFromMemoryCache_withOneCacheEntryAndTwoUrls_returnsOneCacheHitAndOneCacheMiss() throws Exception {
        CacheService.initializeCaches(context);

        assertThat(CacheService.getMemoryLruCache().size()).isEqualTo(0);

        CacheService.putToMemoryCache(url1, imageData1.getBytes());

        Map<String, Bitmap> cacheHits = new HashMap<String, Bitmap>(2);
        List<String> cacheMisses =
                ImageService.getBitmapsFromMemoryCache(Arrays.asList(url1, url2), cacheHits);

        assertThat(cacheHits.keySet()).containsOnly(url1);
        assertThat(cacheMisses).containsOnly(url2);
    }

    private static InputStream getInputStreamFromString(final String string) {
        return spy(new ByteArrayInputStream(string.getBytes()));
    }
}
