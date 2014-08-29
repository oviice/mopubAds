package com.mopub.nativeads;

import com.mopub.nativeads.test.support.SdkTestRunner;

import org.fest.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static com.mopub.nativeads.MoPubNativeAdPositioning.NO_REPEAT;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(manifest=Config.NONE)
@RunWith(SdkTestRunner.class)
public class MoPubNativeAdPositioningTest {

    private MoPubNativeAdPositioning.Builder subject;

    @Before
    public void setup() {
        subject = MoPubNativeAdPositioning.newBuilder();
    }

    @Test
    public void addFixedPositionsOutOfOrder_shouldBeSorted() throws Exception {
        subject.addFixedPosition(27);
        subject.addFixedPosition(31);
        subject.addFixedPosition(17);
        subject.addFixedPosition(7);
        subject.addFixedPosition(56);

        assertThat(subject.build().getFixedPositions())
                .isEqualTo(Lists.newArrayList(7, 17, 27, 31, 56));
    }

    @Test
    public void setRepeatingEnabled_shouldHaveRightInterval() throws Exception {
        subject.addFixedPosition(10);
        subject.enableRepeatingPositions(5);

        assertThat(subject.build().getRepeatingInterval()).isEqualTo(5);
        assertThat(subject.build().getFixedPositions()).isEqualTo(Lists.newArrayList(10));
    }

    @Test
    public void setNoRepeat_shouldReturnNoRepeat() throws Exception {
        subject.enableRepeatingPositions(5);
        subject.enableRepeatingPositions(NO_REPEAT);

        assertThat(subject.build().getRepeatingInterval()).isEqualTo(NO_REPEAT);
    }

    @Test
    public void setAdUnitOverrides_shouldReturnOverrides() throws Exception {
        subject.addFixedPosition(7);
        subject.addFixedPosition(13, "override");

        MoPubNativeAdPositioning underTest = subject.build();
        assertThat(underTest.getAdUnitIdOverride(2)).isNull();  // Nonexistent ad position.
        assertThat(underTest.getAdUnitIdOverride(7)).isNull();
        assertThat(underTest.getAdUnitIdOverride(13)).isEqualTo("override");
    }
    
    @Test
    public void setFixedPositionTwice_shouldReturnOnlyOne() throws Exception {
        subject.addFixedPosition(7, "override");
        subject.addFixedPosition(7);

        MoPubNativeAdPositioning underTest = subject.build();
        assertThat(underTest.getFixedPositions().size()).isEqualTo(1);
        assertThat(underTest.getAdUnitIdOverride(7)).isEqualTo(null);
    }

    @Test
    public void setInvalidFixedPosition_shouldNotAdd() throws Exception {
        subject.addFixedPosition(-3);

        assertThat(subject.build().getFixedPositions().size()).isEqualTo(0);
    }
}
