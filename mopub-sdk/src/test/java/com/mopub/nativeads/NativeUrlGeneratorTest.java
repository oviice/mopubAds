package com.mopub.nativeads;

import android.app.Activity;
import android.net.Uri;

import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class NativeUrlGeneratorTest {
    private Activity context;
    private NativeUrlGenerator subject;

    @Before
    public void setup() {
        context = new Activity();
        shadowOf(context).grantPermissions(ACCESS_NETWORK_STATE);
    }

    @Test
    public void generateNativeAdUrl_shouldIncludeDesiredAssetIfSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId("1234").withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(1);
        assertThat(desiredAssets).contains("title");
    }

    @Test
    public void generateNativeAdUrl_shouldIncludeDesiredAssetsIfSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT, RequestParameters.NativeAdAsset.ICON_IMAGE);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId("1234").withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(3);
        assertThat(desiredAssets).contains("title", "text", "iconimage");
    }

    @Test
    public void generateNativeAdUrl_shouldNotIncludeDesiredAssetsIfNotSet() throws Exception {
        subject = new NativeUrlGenerator(context).withAdUnitId("1234");

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(0);
    }

    @Test
    public void generateNativeAdUrl_shouldNotIncludeDesiredAssetsIfNoAssetsAreSet() throws Exception {
        EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.noneOf(RequestParameters.NativeAdAsset.class);
        RequestParameters requestParameters = new RequestParameters.Builder().desiredAssets(assetsSet).build();

        subject = new NativeUrlGenerator(context).withAdUnitId("1234").withRequest(requestParameters);

        String requestString = generateMinimumUrlString();
        List<String> desiredAssets = getDesiredAssetsListFromRequestUrlString(requestString);

        assertThat(desiredAssets.size()).isEqualTo(0);
    }

    private List<String> getDesiredAssetsListFromRequestUrlString(String requestString) {
        Uri requestUri = Uri.parse(requestString);

        String desiredAssetsString = requestUri.getQueryParameter("assets");
        return (desiredAssetsString == null) ? new ArrayList<String>() : Arrays.asList(desiredAssetsString.split(","));
    }

    private String generateMinimumUrlString() {
        return subject.generateUrlString("ads.mopub.com");
    }
}
