package com.mopub.tests;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.mopub.framework.models.BannerAdLabels;
import com.mopub.framework.pages.AdDetailPage;
import com.mopub.framework.pages.AdListPage;
import com.mopub.framework.pages.AdListPage.AdUnitType;
import com.mopub.simpleadsdemo.R;
import com.mopub.tests.base.MoPubBaseTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MRectAdTests extends MoPubBaseTestCase {

    // Test Variables
    private static final AdUnitType AD_TYPE = AdUnitType.MRECT;
    private static final String TITLE = BannerAdLabels.MRECT;
    private static final String WEB_PAGE_LINK = "https://www.mopub.com/click-test/";

    /*
     * Verify that the MRECT Ad is successfully loaded and displayed on
     * the app.
     */
    @Test
    public void adsDetailsPage_withClickOnMoPubMrectSample_shouldLoadMoPubMrect() {
        final AdListPage adListPage = new AdListPage();
        final AdDetailPage adDetailPage = adListPage.clickCell(TITLE);

        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));

        assertTrue(adDetailPage.waitForElement(bannerElement));
    }

    /*
     * Verify that the MRECT Ad fails to load on the app.
     */
    @Test
    public void adsDetailsPage_withClickOnMoPubMrectSample_shouldNotLoadMoPubMrect() {
        final String fakeAdUnit = "abc";
        final String adUnitTitle = "MRECT Automation Test";

        final AdListPage adListPage = new AdListPage();
        adListPage.addAdUnit(AD_TYPE, fakeAdUnit, adUnitTitle);

        final AdDetailPage adDetailPage = adListPage.clickCell(adUnitTitle);

        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));

        assertTrue(!adDetailPage.waitForElement(bannerElement));

        // Clean Up
        adListPage.deleteAdUnit(adUnitTitle);
    }

    /*
     * Verify that the user is correctly navigated to
     * MRECT Ad's url on click.
     */
    @Test
    public void adsDetailsPage_withClickOnMoPubMrectAd_shouldShowMoPubBrowser() {
        final AdListPage adListPage = new AdListPage();
        final AdDetailPage adDetailPage = adListPage.clickCell(TITLE);

        final ViewInteraction bannerElement = onView(allOf(withId(R.id.banner_mopubview),
                hasChildCount(1)));
        adDetailPage.clickElement(bannerElement);

        final ViewInteraction browserLinkElement = onView(withText(WEB_PAGE_LINK));

        assertTrue(adDetailPage.waitForElement(browserLinkElement));
    }
}
