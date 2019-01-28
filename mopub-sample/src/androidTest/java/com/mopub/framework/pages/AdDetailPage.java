// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.framework.pages;

import com.mopub.framework.base.BasePage;
import com.mopub.simpleadsdemo.R;

public class AdDetailPage extends BasePage {
    public void clickLoadAdButton() {
        clickElementWithId(R.id.load_button);
    }
}
