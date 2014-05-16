package com.mopub.nativeads;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.CacheService;
import com.mopub.common.util.Utils;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class NativeViewHolderTest {
    private Context context;
    private RelativeLayout relativeLayout;
    private ViewGroup viewGroup;
    private NativeResponse nativeResponse;
    private ViewBinder viewBinder;
    private MoPubNative.MoPubNativeListener mopubNativeListener;
    private JSONObject fakeJsonObject;
    private TextView titleView;
    private TextView textView;
    private TextView callToActionView;
    private ImageView mainImageView;
    private ImageView iconImageView;
    private TextView extrasTextView;
    private ImageView extrasImageView;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        relativeLayout = new RelativeLayout(context);
        relativeLayout.setId((int) Utils.generateUniqueId());
        viewGroup = new LinearLayout(context);

        fakeJsonObject = new JSONObject();

        // Only mandatory json fields
        fakeJsonObject.put("imptracker", new JSONArray("[\"url1\", \"url2\"]"));
        fakeJsonObject.put("clktracker", "expected clicktracker");

        // Fields in the web ui
        titleView = new TextView(context);
        titleView.setId((int) Utils.generateUniqueId());
        textView = new TextView(context);
        textView.setId((int) Utils.generateUniqueId());
        callToActionView = new Button(context);
        callToActionView.setId((int) Utils.generateUniqueId());
        mainImageView = new ImageView(context);
        mainImageView.setId((int) Utils.generateUniqueId());
        iconImageView = new ImageView(context);
        iconImageView.setId((int) Utils.generateUniqueId());

        // Extras
        extrasTextView = new TextView(context);
        extrasTextView.setId((int) Utils.generateUniqueId());
        extrasImageView = new ImageView(context);
        extrasImageView.setId((int) Utils.generateUniqueId());

        relativeLayout.addView(titleView);
        relativeLayout.addView(textView);
        relativeLayout.addView(callToActionView);
        relativeLayout.addView(mainImageView);
        relativeLayout.addView(iconImageView);
        relativeLayout.addView(extrasTextView);
        relativeLayout.addView(extrasImageView);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void fromViewBinder_shouldPopulateClassFields() throws Exception {
        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .mainImageId(mainImageView.getId())
                .iconImageId(iconImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(nativeViewHolder.titleView).isEqualTo(titleView);
        assertThat(nativeViewHolder.textView).isEqualTo(textView);
        assertThat(nativeViewHolder.callToActionView).isEqualTo(callToActionView);
        assertThat(nativeViewHolder.mainImageView).isEqualTo(mainImageView);
        assertThat(nativeViewHolder.iconImageView).isEqualTo(iconImageView);
    }

    @Test
    public void fromViewBinder_withSubsetOfFields_shouldLeaveOtherFieldsNull() throws Exception {
        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .iconImageId(iconImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(nativeViewHolder.titleView).isEqualTo(titleView);
        assertThat(nativeViewHolder.textView).isNull();
        assertThat(nativeViewHolder.callToActionView).isNull();
        assertThat(nativeViewHolder.mainImageView).isNull();
        assertThat(nativeViewHolder.iconImageView).isEqualTo(iconImageView);
    }

    @Test
    public void fromViewBinder_withNonExistantIds_shouldLeaveFieldsNull() throws Exception {
        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId((int) Utils.generateUniqueId())
                .textId((int) Utils.generateUniqueId())
                .callToActionId((int) Utils.generateUniqueId())
                .mainImageId((int) Utils.generateUniqueId())
                .iconImageId((int) Utils.generateUniqueId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(nativeViewHolder.titleView).isNull();
        assertThat(nativeViewHolder.textView).isNull();
        assertThat(nativeViewHolder.callToActionView).isNull();
        assertThat(nativeViewHolder.mainImageView).isNull();
        assertThat(nativeViewHolder.iconImageView).isNull();
    }

    @Test
    public void update_shouldAddValuesToViews() throws Exception {
        // Setup for cache state for image gets
        CacheService.initializeCaches(context);
        CacheService.putToMemoryCache("mainimageurl", "mainimagedata".getBytes());
        CacheService.putToMemoryCache("iconimageurl", "iconimagedata".getBytes());

        fakeJsonObject.put("title", "titletext");
        fakeJsonObject.put("text", "texttext");
        fakeJsonObject.put("mainimage", "mainimageurl");
        fakeJsonObject.put("iconimage", "iconimageurl");
        fakeJsonObject.put("ctatext", "cta");
        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .mainImageId(mainImageView.getId())
                .iconImageId(iconImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        nativeViewHolder.update(nativeResponse);

        assertThat(titleView.getText()).isEqualTo("titletext");
        assertThat(textView.getText()).isEqualTo("texttext");
        assertThat(callToActionView.getText()).isEqualTo("cta");
        assertThat(shadowOf(ImageViewServiceTest.getBitmapFromImageView(mainImageView))
                .getCreatedFromBytes()).isEqualTo("mainimagedata".getBytes());
        assertThat(shadowOf(ImageViewServiceTest.getBitmapFromImageView(iconImageView))
                .getCreatedFromBytes()).isEqualTo("iconimagedata".getBytes());
    }

    @Test
    public void update_withMissingNativeResponseFields_shouldClearPreviousValues() throws Exception {
        // Set previous values that should be cleared
        titleView.setText("previoustitletext");
        textView.setText("previoustexttext");
        callToActionView.setText("previousctatext");
        mainImageView.setImageBitmap(ImageService.byteArrayToBitmap("previousmainimagedata".getBytes()));
        iconImageView.setImageBitmap(ImageService.byteArrayToBitmap("previousiconimagedata".getBytes()));

        // Only required fields in native response
        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .mainImageId(mainImageView.getId())
                .iconImageId(iconImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        nativeViewHolder.update(nativeResponse);

        assertThat(titleView.getText()).isEqualTo("");
        assertThat(textView.getText()).isEqualTo("");
        assertThat(callToActionView.getText()).isEqualTo("");
        assertThat(mainImageView.getDrawable()).isNull();
        assertThat(iconImageView.getDrawable()).isNull();
    }

    @Test
    public void update_withDifferentViewBinder_shouldNotClearPreviousValues() throws Exception {
        // Set previous values that should be cleared
        titleView.setText("previoustitletext");
        textView.setText("previoustexttext");

        fakeJsonObject.put("ctatext", "cta");
        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .callToActionId(callToActionView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        nativeViewHolder.update(nativeResponse);

        assertThat(titleView.getText()).isEqualTo("previoustitletext");
        assertThat(textView.getText()).isEqualTo("previoustexttext");
        assertThat(callToActionView.getText()).isEqualTo("cta");
    }

    @Test
    public void updateExtras_shouldAddValuesToViews() throws Exception {
        // Setup for cache state for image gets
        CacheService.initializeCaches(context);
        CacheService.putToMemoryCache("extrasimageurl", "extrasimagedata".getBytes());

        fakeJsonObject.put("extrastext", "extrastexttext");
        fakeJsonObject.put("extrasimage", "extrasimageurl");
        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .addExtra("extrastext", extrasTextView.getId())
                .addExtra("extrasimage", extrasImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        nativeViewHolder.updateExtras(relativeLayout, nativeResponse, viewBinder);

        assertThat(extrasTextView.getText()).isEqualTo("extrastexttext");
        assertThat(shadowOf(ImageViewServiceTest.getBitmapFromImageView(extrasImageView))
                .getCreatedFromBytes()).isEqualTo("extrasimagedata".getBytes());
    }

    @Test
    public void updateExtras_withMissingExtrasValues_shouldClearPreviousValues() throws Exception {
        extrasTextView.setText("previousextrastext");
        extrasImageView.setImageBitmap(ImageService.byteArrayToBitmap("previousextrasimagedata".getBytes()));

        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .addExtra("extrastext", extrasTextView.getId())
                .addExtra("extrasimage", extrasImageView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(extrasTextView.getText()).isEqualTo("previousextrastext");
        assertThat(shadowOf(ImageViewServiceTest.getBitmapFromImageView(extrasImageView))
                .getCreatedFromBytes()).isEqualTo("previousextrasimagedata".getBytes());

        nativeViewHolder.updateExtras(relativeLayout, nativeResponse, viewBinder);

        assertThat(extrasTextView.getText()).isEqualTo("");
        assertThat(extrasImageView.getDrawable()).isNull();
    }

    @Test
    public void updateExtras_withMismatchingViewTypes_shouldNotSetValues() throws Exception {
        fakeJsonObject.put("extrastext", "extrastexttext");
        fakeJsonObject.put("extrasimage", "extrasimageurl");
        nativeResponse = new NativeResponse(fakeJsonObject);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .addExtra("extrastext", extrasImageView.getId())
                .addExtra("extrasimage", extrasTextView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(extrasTextView.getText()).isEqualTo("");
        assertThat(extrasImageView.getDrawable()).isNull();

        nativeViewHolder.updateExtras(relativeLayout, nativeResponse, viewBinder);

        assertThat(extrasTextView.getText()).isEqualTo("");
        assertThat(extrasImageView.getDrawable()).isNull();
    }

    @Test
    public void fromViewBinder_withMixedViewTypes_shouldReturnNull() throws Exception {
        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(mainImageView.getId())
                .textId(textView.getId())
                .build();

        NativeViewHolder nativeViewHolder =
                NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);

        assertThat(nativeViewHolder).isNull();
    }
}
