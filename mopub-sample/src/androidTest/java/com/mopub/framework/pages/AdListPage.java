// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.framework.pages;

import android.support.annotation.NonNull;
import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.mopub.framework.base.BasePage;


public class AdListPage extends BasePage {

    static final String CELL_RESOURCE_NAME = "banner_description";

    public enum AdUnitType {
        BANNER("Banner"),
        MRECT("Mrect"),
        LEADERBOARD("Leaderboard"),
        SKYSCRAPER("Skyscraper"),
        INTERSTITIAL("Interstitial"),
        REWARDED_VIDEO("Rewarded Video"),
        NATIVE_LIST_VIEW("Native List View"),
        NATIVE_RECYCLER_VIEW("Native Recycler View"),
        NATIVE_GALLERY("Native Gallery (Custom Stream)");

        private final String type;

        private AdUnitType(@NonNull final String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }

    public AdDetailPage clickCell(@NonNull final String title) {
        final String failMessage = "This element with resource name '" + CELL_RESOURCE_NAME
                + "' and title '" + title + "' is not present";
        final ViewInteraction element = onView(allOf(withText(title),
                withResourceName(CELL_RESOURCE_NAME)));
        clickElement(element, failMessage);

        return new AdDetailPage();
    }

    public AdListPage addAdUnit(@NonNull final AdUnitType type, @NonNull final String adUnitId, @NonNull final String adUnitName) {
        final String saveAdUnitLabel = "Save ad unit";
        final String adTypeSpinnerResourceId = "add_ad_unit_type";
        final String adUnitIdTextFieldResourceId = "add_ad_unit_id";
        final String adUnitNameTextFieldResourceId = "add_ad_unit_description";

        goToHome();

        clickElementWithText(ADD_AD_UNIT_LABEL);

        final ViewInteraction adTypeSpinner = onView(withResourceName(adTypeSpinnerResourceId));
        adTypeSpinner.perform(click());

        final ViewInteraction adTypeOptionElement = onView(withText(type.getName()))
                .inRoot(isPlatformPopup());
        adTypeOptionElement.perform(click());

        final ViewInteraction adUnitIdTextField = onView(withResourceName(adUnitIdTextFieldResourceId));
        adUnitIdTextField.perform(typeText(adUnitId));

        final ViewInteraction adUnitNameTextField = onView(withResourceName(adUnitNameTextFieldResourceId));
        adUnitNameTextField.perform(typeText(adUnitName));

        clickElementWithText(saveAdUnitLabel);

        return this;
    }

    public AdListPage deleteAdUnit(@NonNull final String adUnitName) {
        final String deleteButtonLabel = "DELETE";

        goToHome();

        final ViewInteraction bannerDeleteElement = onView(allOf(withResourceName("banner_delete"),
                hasSibling(withText(adUnitName))));
        quickClickElement(bannerDeleteElement);

        clickElementWithText(deleteButtonLabel);

        return this;
    }
}
