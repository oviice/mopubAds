package com.mopub.network;

import com.mopub.volley.Cache;
import com.mopub.volley.Network;

public class MoPubRequestQueueTest {

    public static class TestMoPubRequestQueue extends MoPubRequestQueue {

        TestMoPubRequestQueue(Cache cache, Network network) {
            super(cache, network);
        }
    }

    public static class TestMoPubRequestQueue2 extends MoPubRequestQueue {

        TestMoPubRequestQueue2(Cache cache, Network network) {
            super(cache, network);
        }
    }
}
