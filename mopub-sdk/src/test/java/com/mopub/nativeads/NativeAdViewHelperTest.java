package com.mopub.nativeads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mopub.common.DownloadResponse;
import com.mopub.common.util.Utils;
import com.mopub.nativeads.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.mopub.nativeads.MoPubNative.MoPubNativeListener;
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

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        relativeLayout = new RelativeLayout(context);
        relativeLayout.setId((int) Utils.generateUniqueId());
        viewGroup = new LinearLayout(context);

        mNativeAd = new BaseForwardingNativeAd() {};
        mNativeAd.setTitle("test title");
        mNativeAd.setText("test text");
        mNativeAd.setCallToAction("test call to action");

        nativeResponse = new NativeResponse(context, mock(DownloadResponse.class), "testId", mNativeAd, mock(MoPubNativeListener.class));

        titleView = new TextView(context);
        titleView.setId((int) Utils.generateUniqueId());
        textView = new TextView(context);
        textView.setId((int) Utils.generateUniqueId());
        callToActionView = new Button(context);
        callToActionView.setId((int) Utils.generateUniqueId());

        relativeLayout.addView(titleView);
        relativeLayout.addView(textView);
        relativeLayout.addView(callToActionView);

        viewBinder = new ViewBinder.Builder(relativeLayout.getId())
                .titleId(titleView.getId())
                .textId(textView.getId())
                .callToActionId(callToActionView.getId())
                .build();
    }

    @Test
    public void getAdView_shouldReturnPopulatedView() throws Exception {
        View view = NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, nativeResponse, viewBinder, null);

        assertThat(((TextView)view.findViewById(titleView.getId())).getText()).isEqualTo("test title");
        assertThat(((TextView)view.findViewById(textView.getId())).getText()).isEqualTo("test text");
        assertThat(((TextView)view.findViewById(callToActionView.getId())).getText()).isEqualTo("test call to action");

        // not testing images due to testing complexity
    }

    @Test
    public void getAdView_withNullViewBinder_shouldReturnEmptyView() throws Exception {
        View view = NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, nativeResponse, null, null);

        assertThat(view).isNotNull();
        assertThat(view).isNotEqualTo(relativeLayout);
    }

    @Test
    public void getAdView_withNullNativeResponse_shouldReturnGONEConvertView() throws Exception {
        View view = NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, null, viewBinder, null);

        assertThat(view).isEqualTo(relativeLayout);
        assertThat(view.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void getAdView_withDestroyedNativeResponse_shouldReturnGONEConvertView() throws Exception {
        nativeResponse.destroy();
        View view = NativeAdViewHelper.getAdView(relativeLayout, viewGroup, context, nativeResponse, viewBinder, null);

        assertThat(view).isEqualTo(relativeLayout);
        assertThat(view.getVisibility()).isEqualTo(View.GONE);
    }
}
