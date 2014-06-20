package com.mopub.nativeads.test.support;

import com.mopub.common.factories.MethodBuilderFactory;
import com.mopub.common.util.test.support.TestMethodBuilderFactory;
import com.mopub.nativeads.factories.CustomEventNativeFactory;

import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;

public class SdkTestRunner extends RobolectricTestRunner {

    public SdkTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return TestLifeCycleWithInjection.class;
    }

    public static class TestLifeCycleWithInjection extends DefaultTestLifecycle {
        @Override
        public void prepareTest(Object test) {
            MethodBuilderFactory.setInstance(new TestMethodBuilderFactory());
            CustomEventNativeFactory.setInstance(new TestCustomEventNativeFactory());
            MockitoAnnotations.initMocks(test);
        }
    }
}
