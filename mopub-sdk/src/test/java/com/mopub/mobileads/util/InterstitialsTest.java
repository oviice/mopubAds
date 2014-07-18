package com.mopub.mobileads.util;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mopub.common.util.Dips;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.mopub.mobileads.util.Interstitials.addCloseEventRegion;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class InterstitialsTest {

    private Activity context;
    private RelativeLayout.LayoutParams defaultLayoutParams;

    @Before
    public void setup() {
        context = new Activity();
        defaultLayoutParams = new RelativeLayout.LayoutParams(50, 50);
    }

    @Test
    public void addCloseEventRegion_withNullViewGroup_shouldReturnFalse() throws Exception {
        boolean result = addCloseEventRegion(null, defaultLayoutParams, null);

        assertThat(result).isFalse();
    }

    @Test
    public void addCloseEventRegion_withViewGroupButNoContext_shouldReturnFalse() throws Exception {
        ViewGroup viewGroup = mock(ViewGroup.class);
        when(viewGroup.getContext()).thenReturn(null);

        boolean result = addCloseEventRegion(viewGroup, defaultLayoutParams, null);

        assertThat(result).isFalse();
    }

    @Test
    public void addCloseEventRegion_shouldAddTransparentButVisibleButton() throws Exception {
        final LinearLayout linearLayout = new LinearLayout(context);

        addCloseEventRegion(linearLayout, defaultLayoutParams, null);

        final Button closeEventRegion = (Button) linearLayout.getChildAt(0);
        assertThat(closeEventRegion.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(shadowOf(closeEventRegion).getBackgroundColor()).isEqualTo(Color.TRANSPARENT);
        assertThat(Dips.pixelsToIntDips((float) closeEventRegion.getLayoutParams().width, context)).isEqualTo(50);
        assertThat(Dips.pixelsToIntDips((float)closeEventRegion.getLayoutParams().height, context)).isEqualTo(50);
    }

    @Test
    public void addCloseEventRegion_shouldUsePassedInClickListener() throws Exception {
        final LinearLayout linearLayout = new LinearLayout(context);
        final View.OnClickListener onClickListener = mock(View.OnClickListener.class);

        addCloseEventRegion(linearLayout, defaultLayoutParams, onClickListener);

        final Button closeEventRegion = (Button) linearLayout.getChildAt(0);
        assertThat(shadowOf(closeEventRegion).getOnClickListener()).isEqualTo(onClickListener);
    }
}
