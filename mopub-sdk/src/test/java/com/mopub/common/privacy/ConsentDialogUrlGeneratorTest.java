package com.mopub.common.privacy;

import android.app.Activity;
import android.content.Context;

import com.mopub.common.ClientMetadata;
import com.mopub.common.Constants;
import com.mopub.common.MoPub;
import com.mopub.mobileads.BuildConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest(ClientMetadata.class)
public class ConsentDialogUrlGeneratorTest {
    private static final String AD_UNIT_ID = "ad_unit_id";
    private static final String CONSENT_STATUS_UNKNOWN = ConsentStatus.UNKNOWN.getValue();
    private static final String VERSION = MoPub.SDK_VERSION;
    private static final String CURRENT_LANGUAGE = "current_language";
    private static final String FORCE_GDPR_APPLIES = "0";
    private static final String BUNDLE = "test.bundle";

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private Context context;
    private ConsentDialogUrlGenerator subject;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(Activity.class).get();
        context = activity.getApplicationContext();

        PowerMockito.mockStatic(ClientMetadata.class);
        PowerMockito.when(ClientMetadata.getCurrentLanguage(context)).thenReturn(CURRENT_LANGUAGE);
        ClientMetadata mockClientMetadata = PowerMockito.mock(ClientMetadata.class);
        PowerMockito.when(mockClientMetadata.getAppPackageName()).thenReturn(BUNDLE);
        PowerMockito.when(ClientMetadata.getInstance(any(Context.class))).thenReturn(
                mockClientMetadata);
    }

    @Test
    public void generateUrlString_withValidAdUnitId_shouldGenerateValidUrl() {
        String validUrl = createTestUrl();

        subject = new ConsentDialogUrlGenerator(context, AD_UNIT_ID,
                ConsentStatus.UNKNOWN.getValue());
        String url = subject.generateUrlString(Constants.HOST);

        assertThat(url).isEqualTo(validUrl);
    }

    // unit test utils
    private String createTestUrl() {
        return "https://" + Constants.HOST + "/m/gdpr_consent_dialog" +
                "?id=" + AD_UNIT_ID +
                "&current_consent_status=" + CONSENT_STATUS_UNKNOWN +
                "&nv=" + VERSION +
                "&language=" + CURRENT_LANGUAGE +
                "&force_gdpr_applies=" + FORCE_GDPR_APPLIES +
                "&bundle=" + BUNDLE;
    }
}
