package com.mopub.nativeads;

import android.app.Activity;

import com.mopub.common.CacheService;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.apache.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.RequestMatcher;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.util.HashMap;

import static com.mopub.common.util.test.support.CommonUtils.assertHttpRequestsMade;
import static com.mopub.nativeads.CustomEventNative.CustomEventNativeListener;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class MoPubCustomEventNativeTest {

    private MoPubCustomEventNative subject;
    private Activity context;
    private HashMap<String, Object> localExtras;
    private CustomEventNativeListener mCustomEventNativeListener;
    private HashMap<String, String> serverExtras;
    private JSONObject fakeJsonObject;
    private FakeHttpLayer fakeHttpLayer;

    @Before
    public void setUp() throws Exception {
        subject = new MoPubCustomEventNative();
        context = new Activity();

        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();

        fakeJsonObject = new JSONObject();
        fakeJsonObject.put("imptracker", new JSONArray("[\"url1\", \"url2\"]"));
        fakeJsonObject.put("clktracker", "expected clicktracker");
        fakeJsonObject.put("mainimage", "mainimageurl");
        fakeJsonObject.put("iconimage", "iconimageurl");
        fakeJsonObject.put("extraimage", "extraimageurl");

        serverExtras.put(CustomEventNativeAdapter.RESPONSE_BODY_KEY, fakeJsonObject.toString());

        mCustomEventNativeListener = mock(CustomEventNativeListener.class);

        fakeHttpLayer = Robolectric.getFakeHttpLayer();
        fakeHttpLayer.addHttpResponseRule(
                new RequestMatcher() {
                    @Override
                    public boolean matches(HttpRequest request) {
                        return true;
                    }
                },
                new TestHttpResponse(200, "body")
        );
    }

    @After
    public void tearDown() throws Exception {
        CacheService.clearAndNullCaches();
        reset(mCustomEventNativeListener);
    }

    @Test
    public void loadNativeAd_withValidInput_shouldDownloadImagesAndNotifyListenerOfOnNativeAdLoaded() throws Exception {
        subject.loadNativeAd(context, mCustomEventNativeListener, localExtras, serverExtras);

        assertHttpRequestsMade(null, "mainimageurl", "iconimageurl", "extraimageurl");

        verify(mCustomEventNativeListener).onNativeAdLoaded(any(MoPubCustomEventNative.MoPubForwardingNativeAd.class));
        verify(mCustomEventNativeListener, never()).onNativeAdFailed(any(NativeErrorCode.class));
    }

    @Test
    public void loadNativeAd_withValidInput_withFailedImageDownload_shouldNotDownloadImagesAndNotifyListenerOfOnNativeAdFailed() throws Exception {
        fakeHttpLayer.clearHttpResponseRules();
        fakeHttpLayer.addPendingHttpResponse(500, "body");

        serverExtras.put(CustomEventNativeAdapter.RESPONSE_BODY_KEY, fakeJsonObject.toString());

        subject.loadNativeAd(context, mCustomEventNativeListener, localExtras, serverExtras);

        verify(mCustomEventNativeListener, never()).onNativeAdLoaded(any(MoPubCustomEventNative.MoPubForwardingNativeAd.class));
        verify(mCustomEventNativeListener).onNativeAdFailed(NativeErrorCode.IMAGE_DOWNLOAD_FAILURE);
    }

    @Test
    public void loadNativeAd_withInvalidResponseBody_shouldNotifyListenerOfOnNativeAdFailedAndReturn() throws Exception {
        serverExtras.put(CustomEventNativeAdapter.RESPONSE_BODY_KEY, "{ \"bad json");

        subject.loadNativeAd(context, mCustomEventNativeListener, localExtras, serverExtras);
        verify(mCustomEventNativeListener, never()).onNativeAdLoaded(any(MoPubCustomEventNative.MoPubForwardingNativeAd.class));
        verify(mCustomEventNativeListener).onNativeAdFailed(NativeErrorCode.INVALID_JSON);
    }

    @Test
    public void loadNativeAd_withNullResponseBody_shouldNotifyListenerOfOnNativeAdFailedAndReturn() throws Exception {
        serverExtras.put(CustomEventNativeAdapter.RESPONSE_BODY_KEY, null);

        subject.loadNativeAd(context, mCustomEventNativeListener, localExtras, serverExtras);
        verify(mCustomEventNativeListener, never()).onNativeAdLoaded(any(MoPubCustomEventNative.MoPubForwardingNativeAd.class));
        verify(mCustomEventNativeListener).onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
    }
}
