package com.mopub.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import com.mopub.common.MoPubBrowser;
import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidVideoPlayerActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowToast;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ManifestUtilsTest {
    private Context context;
    private ResolveInfo resolveInfo;
    private List<Class<? extends Activity>> requiredWebViewSdkActivities;
    private List<Class<? extends Activity>> requiredNativeSdkActivities;

    @Before
    public void setUp() throws Exception {
        context = spy(new Activity());
        resolveInfo = mock(ResolveInfo.class);

        requiredWebViewSdkActivities = ManifestUtils.getRequiredWebViewSdkActivities();
        requiredNativeSdkActivities = ManifestUtils.getRequiredNativeSdkActivities();

        setDebugMode(false);
    }

    @After
    public void tearDown() throws Exception {
        setDebugMode(false);
    }

    @Test
    public void checkWebViewSdkActivitiesDeclared_shouldIncludeFourActivityDeclarations() throws Exception {
        ShadowLog.setupLogging();

        ManifestUtils.checkWebViewActivitiesDeclared(context);

        assertLogIncludes(
                "com.mopub.mobileads.MoPubActivity",
                "com.mopub.mobileads.MraidActivity",
                "com.mopub.mobileads.MraidVideoPlayerActivity",
                "com.mopub.common.MoPubBrowser"
        );
    }

    @Test
    public void checkNativeSdkActivitiesDeclared_shouldIncludeOneActivityDeclaration() throws Exception {
        ShadowLog.setupLogging();

        ManifestUtils.checkNativeActivitiesDeclared(context);

        assertLogIncludes("com.mopub.common.MoPubBrowser");
        assertLogDoesntInclude(
                "com.mopub.mobileads.MoPubActivity",
                "com.mopub.mobileads.MraidActivity",
                "com.mopub.mobileads.MraidVideoPlayerActivity"
        );
    }

    @Test
    public void displayWarningForMissingActivities_withAllActivitiesDeclared_shouldNotShowLogOrToast() throws Exception {
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MoPubActivity.class), resolveInfo);
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MraidActivity.class), resolveInfo);
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MraidVideoPlayerActivity.class), resolveInfo);
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MoPubBrowser.class), resolveInfo);

        ShadowLog.setupLogging();
        setDebugMode(true);

        ManifestUtils.displayWarningForMissingActivities(context, requiredWebViewSdkActivities);

        assertThat(ShadowToast.getLatestToast()).isNull();
        assertThat(ShadowLog.getLogs()).isEmpty();
    }

    @Test
     public void displayWarningForMissingActivities_withOneMissingActivity_shouldLogOnlyThatOne() throws Exception {
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MoPubActivity.class), resolveInfo);
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MraidActivity.class), resolveInfo);
        Robolectric.packageManager.addResolveInfoForIntent(new Intent(context, MraidVideoPlayerActivity.class), resolveInfo);
        // Here, we leave out MoPubBrowser on purpose

        ShadowLog.setupLogging();

        ManifestUtils.displayWarningForMissingActivities(context, requiredWebViewSdkActivities);

        assertLogIncludes("com.mopub.common.MoPubBrowser");
        assertLogDoesntInclude(
                "com.mopub.mobileads.MoPubActivity",
                "com.mopub.mobileads.MraidActivity",
                "com.mopub.mobileads.MraidVideoPlayerActivity"
        );
    }

    @Test
    public void displayWarningForMissingActivities_withMissingActivities_withDebugTrue_shouldShowToast() throws Exception {
        setDebugMode(true);

        ManifestUtils.displayWarningForMissingActivities(context, requiredWebViewSdkActivities);

        assertThat(ShadowToast.getLatestToast()).isNotNull();
        final String toastText = ShadowToast.getTextOfLatestToast();
        assertThat(toastText).isEqualTo("ERROR: YOUR MOPUB INTEGRATION IS INCOMPLETE.\nCheck logcat and update your AndroidManifest.xml with the correct activities.");
    }

    @Test
    public void displayWarningForMissingActivities_withMissingActivities_withDebugFalse_shouldNotShowToast() throws Exception {
        setDebugMode(false);

        ManifestUtils.displayWarningForMissingActivities(context, requiredWebViewSdkActivities);

        assertThat(ShadowToast.getLatestToast()).isNull();
    }
    
    @Test
    public void displayWarningForMissingActivities_withMissingActivities_withDebugTrue_shouldLogMessage() throws Exception {
        setDebugMode(true);
        ShadowLog.setupLogging();

        ManifestUtils.displayWarningForMissingActivities(context, requiredWebViewSdkActivities);

        final List<ShadowLog.LogItem> logs = ShadowLog.getLogs();

        assertLogIncludes(
                "com.mopub.mobileads.MoPubActivity",
                "com.mopub.mobileads.MraidActivity",
                "com.mopub.mobileads.MraidVideoPlayerActivity",
                "com.mopub.common.MoPubBrowser"
        );
    }

    @Test
    public void isDebuggable_whenApplicationIsDebuggable_shouldReturnTrue() throws Exception {
        setDebugMode(true);

        assertThat(ManifestUtils.isDebuggable(context)).isTrue();
    }

    @Test
    public void isDebuggable_whenApplicationIsNotDebuggable_shouldReturnFalse() throws Exception {
        setDebugMode(false);

        assertThat(ManifestUtils.isDebuggable(context)).isFalse();
    }

    @Test
    public void getRequiredWebViewSdkActivities_shouldIncludeRequiredActivities() throws Exception {
        assertThat(requiredWebViewSdkActivities).containsOnly(
                MoPubActivity.class,
                MraidActivity.class,
                MraidVideoPlayerActivity.class,
                MoPubBrowser.class
        );
    }

    @Test
    public void getRequiredNativeSdkActivities_shouldIncludeRequiredActivities() throws Exception {
        assertThat(requiredNativeSdkActivities).containsOnly(
                MoPubBrowser.class
        );
    }

    private void setDebugMode(boolean enabled) {
        final ApplicationInfo applicationInfo = context.getApplicationInfo();

        if (enabled) {
            applicationInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
        } else {
            applicationInfo.flags &= ~ApplicationInfo.FLAG_DEBUGGABLE;
        }

        when(context.getApplicationInfo()).thenReturn(applicationInfo);
    }

    private void assertLogIncludes(final String... messages) {
        final String logText = ShadowLog.getLogs().get(0).msg;
        for (final String message : messages) {
            assertThat(logText).containsOnlyOnce(message);
        }
    }

    private void assertLogDoesntInclude(final String... messages) {
        final String logText = ShadowLog.getLogs().get(0).msg;
        for (final String message : messages) {
            assertThat(logText).doesNotContain(message);
        }
    }
}
