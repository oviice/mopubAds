// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.tests.base;

import android.support.test.espresso.intent.rule.IntentsTestRule;

import com.mopub.simpleadsdemo.MoPubSampleActivity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;

public class MoPubBaseTestCase {

    @Rule
    public IntentsTestRule<MoPubSampleActivity> mActivityRule =
            new IntentsTestRule<MoPubSampleActivity>(MoPubSampleActivity.class);

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
}
