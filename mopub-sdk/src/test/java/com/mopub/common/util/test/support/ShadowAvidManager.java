// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.common.util.test.support;

import android.app.Activity;

import com.integralads.avid.library.mopub.AvidManager;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@Implements(value = AvidManager.class)
public class ShadowAvidManager {
    @Mock private static AvidManager mockAvidManager;
    private static Activity sLatestRegisteredActivity;

    static {
        resetManager();
    }

    public static void reset() {
        resetManager();

        sLatestRegisteredActivity = null;
    }

    @Implementation
    public static AvidManager getInstance() {
        return mockAvidManager;
    }

    public static Activity getLatestRegisteredActivity() {
        return sLatestRegisteredActivity;
    }

    private static void resetManager() {
        mockAvidManager = mock(AvidManager.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                sLatestRegisteredActivity = (Activity) invocation.getArguments()[0];
                return null;
            }
        }).when(mockAvidManager).registerActivity(any(Activity.class));
    }
}
