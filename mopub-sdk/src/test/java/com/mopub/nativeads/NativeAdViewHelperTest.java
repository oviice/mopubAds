package com.mopub.nativeads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.DownloadResponse;
import com.mopub.common.util.ResponseHeader;
import com.mopub.common.util.Utils;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static com.mopub.nativeads.MoPubNative.MoPubNativeListener;
import static com.mopub.nativeads.NativeAdViewHelper.NativeViewClickListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SdkTestRunner.class)
public class NativeAdViewHelperTest {
    private Activity context;
    private RelativeLayout relativeLayout;
    private ViewGroup viewGroup;
    private NativeResponse nativeResponse;
    private BaseForwardingNativeAd mNativeAd;
    private ViewBinder viewBinder;
    private TextView titleView;
    private TextView textView;
    private TextView callToActionView;
    private ImageView mainImageView;
    private ImageView iconImageView;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        relativeLayout = new RelativeLayout(context);
        relativeLayout.setId((int) Utils.generateUniqueId());
        viewGroup = new LinearLayout(context);

        mNativeAd = new BaseForwardingNativeAd() {};
        mNativeAd.setClickDestinationUrl("destinationUrl");
        final TestHttpResponseWithHeaders testHttpResponseWithHeaders = new TestHttpResponseWithHeaders(200, "");
        testHttpResponseWithHeaders.addHeader(ResponseHeader.CLICKTHROUGH_URL.getKey(), "clickTrackerUrl");
        final DownloadResponse downloadResponse = new DownloadResponse(testHttpResponseWithHeaders);
        nativeResponse = new NativeResponse(context, downloadResponse, mNativeAd, mock(MoPubNativeListener.class));

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

        relativeLayout.addView(titleView);
        relativeLayout.addView(textView);
        relativeLayout.addView(callToActionView);
        relativeLayout.addView(mainImageView);
        relativeLayout.addView(iconImageView);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .mainImageId(mainImageView.getId())
                .iconImageId(iconImageView.getId())
                .build();
    }

    @Test
    public void getAdView_whenCallToActionIsAButton_shouldAttachClickListenersToConvertViewAndCtaButton() throws Exception {
        assertThat(relativeLayout.performClick()).isFalse();
        assertThat(callToActionView.performClick()).isFalse();
        NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, nativeResponse, viewBinder, null);
        assertThat(relativeLayout.performClick()).isTrue();
        assertThat(callToActionView.performClick()).isTrue();
    }

    @Test
    public void getAdView_whenCallToActionIsATextView_shouldAttachClickListenersToConvertViewOnly() throws Exception {
        relativeLayout.removeView(callToActionView);
        callToActionView = new TextView(context);
        callToActionView.setId((int) Utils.generateUniqueId());
        relativeLayout.addView(callToActionView);

        assertThat(relativeLayout.performClick()).isFalse();
        assertThat(callToActionView.performClick()).isFalse();

        NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, nativeResponse, viewBinder, null);

        assertThat(relativeLayout.performClick()).isTrue();
        assertThat(callToActionView.performClick()).isFalse();
    }

    @Test
    public void getOrCreateNativeViewHolder_withNoViewHolder_shouldCreateNativeViewHolder() throws Exception {
        assertThat(ImageViewService.getViewTag(relativeLayout)).isNull();
        NativeAdViewHelper.getOrCreateNativeViewHolder(relativeLayout, viewBinder);
        final NativeViewHolder nativeViewHolder = NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);
        compareNativeViewHolders(nativeViewHolder, (NativeViewHolder) ImageViewService.getViewTag(relativeLayout));
    }

    @Test
    public void getOrCreateNativeViewHolder_whenViewTagHasOtherObject_shouldCreateNativeViewHolder() throws Exception {
        assertThat(ImageViewService.getViewTag(relativeLayout)).isNull();
        ImageViewService.setViewTag(relativeLayout, new Object());
        NativeAdViewHelper.getOrCreateNativeViewHolder(relativeLayout, viewBinder);
        final NativeViewHolder nativeViewHolder = NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);
        compareNativeViewHolders(nativeViewHolder, (NativeViewHolder) ImageViewService.getViewTag(relativeLayout));
    }

    @Test
    public void getOrCreateNativeViewHolder_whenViewTagHasNativeViewHolder_shouldNotCreateNativeViewHolder() throws Exception {
        assertThat(ImageViewService.getViewTag(relativeLayout)).isNull();
        final NativeViewHolder nativeViewHolder = NativeViewHolder.fromViewBinder(relativeLayout, viewBinder);
        ImageViewService.setViewTag(relativeLayout, nativeViewHolder);
        NativeAdViewHelper.getOrCreateNativeViewHolder(relativeLayout, viewBinder);
        assertThat(ImageViewService.getViewTag(relativeLayout)).isEqualTo(nativeViewHolder);
    }

    @Test
    public void onClick_shouldQueueClickTrackerAndUrlResolutionTasks() throws Exception {
        NativeViewClickListener nativeViewClickListener = new NativeViewClickListener(nativeResponse);

        Robolectric.getBackgroundScheduler().pause();
        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(0);
        nativeViewClickListener.onClick(new View(context));

        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(2);
    }

    @Test
    public void onClick_withNullDestinationUrl_shouldNotQueueUrlResolutionTask() throws Exception {
        mNativeAd.setClickDestinationUrl(null);

        NativeViewClickListener nativeViewClickListener = new NativeViewClickListener(nativeResponse);

        Robolectric.getBackgroundScheduler().pause();
        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(0);
        nativeViewClickListener.onClick(new View(context));

        // 1 task for async ping to click tracker
        assertThat(Robolectric.getBackgroundScheduler().enqueuedTaskCount()).isEqualTo(1);
    }

    static private void compareNativeViewHolders(final NativeViewHolder nativeViewHolder1,
                                                 final NativeViewHolder nativeViewHolder2) {
        assertThat(nativeViewHolder1.titleView).isEqualTo(nativeViewHolder2.titleView);
        assertThat(nativeViewHolder1.textView).isEqualTo(nativeViewHolder2.textView);
        assertThat(nativeViewHolder1.callToActionView).isEqualTo(nativeViewHolder2.callToActionView);
        assertThat(nativeViewHolder1.mainImageView).isEqualTo(nativeViewHolder2.mainImageView);
        assertThat(nativeViewHolder1.iconImageView).isEqualTo(nativeViewHolder2.iconImageView);
    }
}
