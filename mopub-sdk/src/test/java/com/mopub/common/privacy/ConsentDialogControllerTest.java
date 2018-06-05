package com.mopub.common.privacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.SupportActivity;

import com.mopub.common.Constants;
import com.mopub.common.util.Intents;
import com.mopub.common.util.Reflection;
import com.mopub.mobileads.BuildConfig;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.network.MoPubNetworkError;
import com.mopub.network.MoPubRequestQueue;
import com.mopub.network.MoPubRequestQueueTest;
import com.mopub.network.Networking;
import com.mopub.volley.VolleyError;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static com.mopub.network.MoPubNetworkError.Reason.BAD_BODY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({Networking.class, Intents.class})
public class ConsentDialogControllerTest {
    private static final String AD_UNIT_ID = "ad_unit_id";
    private static final String MREADY = "mReady";
    private static final String REQUEST_IN_FLIGHT = "mRequestInFlight";

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private ConsentDialogResponse dialogResponse;
    private ConsentDialogController subject;
    private PersonalInfoData personalInfoData;

    // mock objects
    private MoPubRequestQueue mockRequestQueue;
    private ConsentDialogListener mockDialogListener;

    @Before
    public void setup() {
        Activity activity = Robolectric.buildActivity(SupportActivity.class).get();
        Context context = activity.getApplicationContext();
        mockRequestQueue = Mockito.mock(MoPubRequestQueueTest.TestMoPubRequestQueue.class);
        mockDialogListener = Mockito.mock(ConsentDialogListener.class);
        dialogResponse = new ConsentDialogResponse("html_text");
        personalInfoData = new PersonalInfoData(context, AD_UNIT_ID);

        PowerMockito.mockStatic(Networking.class);
        when(Networking.getRequestQueue(context)).thenReturn(mockRequestQueue);
        when(Networking.getScheme()).thenReturn(Constants.HTTPS);

        PowerMockito.mockStatic(Intents.class);

        subject = new ConsentDialogController(context);
    }

    @Test
    public void loadConsentDialog_whenReadyIsFalse_whenRequestInFlightIsFalse_shouldAddRequestToNetworkQueue() throws Exception {
        ArgumentCaptor<ConsentDialogRequest> requestCaptor = ArgumentCaptor.forClass(ConsentDialogRequest.class);

        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);

        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isTrue();
        verify(mockDialogListener, never()).onConsentDialogLoaded();
        verify(mockRequestQueue).add(requestCaptor.capture());
        ConsentDialogRequest request = requestCaptor.getValue();
        assertThat(request.getUrl().indexOf(Constants.GDPR_CONSENT_HANDLER)).isNotNegative();
    }

    @Test
    public void loadConsentDialog_whenReadyIsTrue_whenRequestInFlightIsFalse_shouldNotAddRequestToNetworkQueue() throws Exception {
        setFlagReady(subject);

        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);

        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockDialogListener).onConsentDialogLoaded(); // should call listener immediately
        verify(mockRequestQueue, never()).add(any(ConsentDialogRequest.class));
    }

    @Test
    public void loadConsentDialog_whenReadyIsTrue_witListenerNotSet_shouldNotCrash() throws Exception {
        setFlagReady(subject);

        subject.loadConsentDialog(null, true, personalInfoData);

        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockRequestQueue, never()).add(any(ConsentDialogRequest.class));
    }

    @Test
    public void loadConsentDialog_whenRequestInFlightIsTrue_shouldNotCreateNewRequest_shouldNotCallListener() throws Exception {
        setFlagRequestInFlight(subject);

        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);

        verify(mockDialogListener, never()).onConsentDialogLoaded();
        verify(mockRequestQueue, never()).add(any(ConsentDialogRequest.class));
    }

    @Test
    public void onSuccess_withValidResponse_shouldCallConsentDialogLoaded() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onSuccess(dialogResponse);

        assertThat(getFlag(subject, MREADY)).isTrue();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockDialogListener).onConsentDialogLoaded();
        verify(mockDialogListener, never()).onConsentDialogLoadFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void onSuccess_withEmptyResponse_shouldNotCallConsentDialogLoaded() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onSuccess(new ConsentDialogResponse(""));

        assertThat(getFlag(subject, MREADY)).isFalse();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockDialogListener, never()).onConsentDialogLoaded();
        verify(mockDialogListener).onConsentDialogLoadFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void onErrorResponse_shouldResetState_shouldCallDialogFailed() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onErrorResponse(new VolleyError());

        assertThat(getFlag(subject, MREADY)).isFalse();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockDialogListener, never()).onConsentDialogLoaded();
        verify(mockDialogListener).onConsentDialogLoadFailed(any(MoPubErrorCode.class));
    }

    @Test
    public void onErrorResponse_withErrorBadBody_shouldResetState_shouldCallDialogFailed() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onErrorResponse(new MoPubNetworkError(BAD_BODY));

        assertThat(getFlag(subject, MREADY)).isFalse();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verify(mockDialogListener, never()).onConsentDialogLoaded();
        verify(mockDialogListener).onConsentDialogLoadFailed(MoPubErrorCode.INTERNAL_ERROR);
    }

    @Test
    public void showConsentDialog_whenDataIsReady_shouldStartActivity_shouldResetControllerState() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onSuccess(dialogResponse);

        subject.showConsentDialog();

        assertThat(getFlag(subject, MREADY)).isFalse();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verifyStatic();
        Intents.startActivity(any(Context.class), any(Intent.class));
    }

    @Test
    public void showConsentDialog_whenDataIsNotReady_shouldNotStartActivity() throws Exception {
        subject.loadConsentDialog(mockDialogListener, true, personalInfoData);
        subject.onErrorResponse(new MoPubNetworkError(BAD_BODY));

        subject.showConsentDialog();

        assertThat(getFlag(subject, MREADY)).isFalse();
        assertThat(getFlag(subject, REQUEST_IN_FLIGHT)).isFalse();
        verifyStatic(never());
        Intents.startActivity(any(Context.class), any(Intent.class));
    }

    // test utils
    private static void setFlagReady(ConsentDialogController target) throws Exception {
        Field field = Reflection.getPrivateField(ConsentDialogController.class, MREADY);
        field.setBoolean(target, true);
    }

    private static void setFlagRequestInFlight(ConsentDialogController target) throws Exception {
        Field field = Reflection.getPrivateField(ConsentDialogController.class, REQUEST_IN_FLIGHT);
        field.setBoolean(target, true);
    }

    private static boolean getFlag(ConsentDialogController target, final String fieldName) throws Exception {
        Field field = Reflection.getPrivateField(ConsentDialogController.class, fieldName);
        return field.getBoolean(target);
    }
}
