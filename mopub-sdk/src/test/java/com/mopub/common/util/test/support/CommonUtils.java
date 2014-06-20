package com.mopub.common.util.test.support;

import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;

public class CommonUtils {
    public static void assertHttpRequestsMade(String... urls) {
        final int numberOfReceivedHttpRequests = Robolectric.getFakeHttpLayer().getSentHttpRequestInfos().size();
        assertThat(numberOfReceivedHttpRequests).isEqualTo(urls.length);

        for (final String url : urls) {
            assertThat(Robolectric.httpRequestWasMade(url)).isTrue();
        }
    }
}
