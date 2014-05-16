package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.mopub.common.CacheService;
import com.mopub.common.CacheServiceTest;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class ImageViewServiceTest {

    private ImageView imageView;
    private String url1;
    private String url2;
    private String imageData1;
    private String imageData2;
    private FakeHttpLayer fakeHttpLayer;

    @Before
    public void setUp() throws Exception {
        Context context = new Activity();
        imageView = new ImageView(context);
        CacheService.initializeCaches(context);
        url1 = "http://www.mopub.com/";
        url2 = "http://www.twitter.com/";
        imageData1 = "image_data_1";
        imageData2 = "image_data_2";
        fakeHttpLayer = Robolectric.getFakeHttpLayer();
    }

    @After
    public void tearDown() throws Exception {
        CacheService.clearAndNullCaches();
    }

    @Test
    public void loadImageView_withImageInMemoryCache_shouldLoadImageData() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        CacheService.putToMemoryCache(url1, imageData1.getBytes());

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        ImageViewService.loadImageView(url1, imageView);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNotNull();
        assertThat(shadowOf(getBitmapFromImageView(imageView)).getCreatedFromBytes())
                .isEqualTo(imageData1.getBytes());
    }

    @Test
    public void loadImageView_withImageInDiskCache_shouldLoadImageDataAsync() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        CacheService.putToDiskCache(url1, imageData1.getBytes());

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        ImageViewService.loadImageView(url1, imageView);
        Thread.sleep(500);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNotNull();
        assertThat(shadowOf(getBitmapFromImageView(imageView)).getCreatedFromBytes())
                .isEqualTo(imageData1.getBytes());
    }

    @Test
    public void loadImageView_withImageInNetwork_shouldLoadImageDataAsync() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        ImageViewService.loadImageView(url1, imageView);
        Thread.sleep(500);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNotNull();
        assertThat(shadowOf(getBitmapFromImageView(imageView)).getCreatedFromBytes())
                .isEqualTo(imageData1.getBytes());
    }

    @Test
    public void loadImageView_withImageInNetworkAndUniqueIdChanges_shouldNotLoadImageData() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        assertThat(imageView.getDrawable()).isNull();
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        Robolectric.getBackgroundScheduler().pause();
        ImageViewService.loadImageView(url1, imageView);

        // Change unique id before running async task to simulate another image load
        ImageViewService.setImageViewUniqueId(imageView);

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getBackgroundScheduler().unPause();
        Thread.sleep(500);

        assertThat(imageView.getDrawable()).isNull();
    }

    @Test
    public void loadImageView_withImageInNetworkAndUniqueIdIsNull_shouldNotLoadImageData() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        assertThat(imageView.getDrawable()).isNull();
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        Robolectric.getBackgroundScheduler().pause();
        ImageViewService.loadImageView(url1, imageView);

        // Change unique id before running async task to simulate another image load
        ImageViewService.setViewTag(imageView, null);

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getBackgroundScheduler().unPause();
        Thread.sleep(500);

        assertThat(imageView.getDrawable()).isNull();
    }

    @Test
    public void loadImageView_withTwoNetworkRequests_shouldLoadSecondImageData() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        assertThat(imageView.getDrawable()).isNull();
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);
        fakeHttpLayer.addPendingHttpResponse(200, imageData2);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        Robolectric.getBackgroundScheduler().pause();
        ImageViewService.loadImageView(url1, imageView);
        ImageViewService.loadImageView(url2, imageView);

        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getBackgroundScheduler().runOneTask();
        Robolectric.getBackgroundScheduler().unPause();
        Thread.sleep(500);

        assertThat(shadowOf(getBitmapFromImageView(imageView)).getCreatedFromBytes())
                .isEqualTo(imageData2.getBytes());
    }

    @Test
    public void loadImageView_shouldClearDrawable() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        assertThat(imageView.getDrawable()).isNull();
        fakeHttpLayer.addPendingHttpResponse(200, imageData1);
        fakeHttpLayer.addPendingHttpResponse(200, imageData2);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNull();

        ImageViewService.loadImageView(url1, imageView);
        Thread.sleep(500);
        assertThat(shadowOf(getBitmapFromImageView(imageView)).getCreatedFromBytes())
                .isEqualTo(imageData1.getBytes());

        Robolectric.getBackgroundScheduler().pause();
        ImageViewService.loadImageView(url2, imageView);
        assertThat(imageView.getDrawable()).isNull();
    }

    @Test
    public void loadImageView_withEmptyCachesAndNetworkFailure_shouldNotLoadImageDataAsync() throws Exception {
        CacheServiceTest.assertCachesAreEmpty();
        fakeHttpLayer.addPendingHttpResponse(500, imageData1);

        ImageViewService.loadImageView(url1, imageView);
        Thread.sleep(500);

        assertThat(ImageViewService.getImageViewUniqueId(imageView)).isNotNull();
        assertThat(imageView.getDrawable()).isNull();
    }

    static Bitmap getBitmapFromImageView(final ImageView imageView) {
        return ((BitmapDrawable)imageView.getDrawable()).getBitmap();
    }
}
