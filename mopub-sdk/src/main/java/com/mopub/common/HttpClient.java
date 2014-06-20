package com.mopub.common;

import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.os.Looper;

import com.mopub.common.util.AsyncTasks;
import com.mopub.common.util.DeviceUtils;
import com.mopub.common.util.MoPubLog;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.Arrays;

public class HttpClient {
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 10000;

    public static AndroidHttpClient getHttpClient() {
        String userAgent = DeviceUtils.getUserAgent();

        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(userAgent);

        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);
        HttpClientParams.setRedirecting(params, true);

        return httpClient;
    }

    public static void makeTrackingHttpRequest(final Iterable<String> urls) {
        if (urls == null) {
            return;
        }

        final DownloadTask.DownloadTaskListener downloadTaskListener = new DownloadTask.DownloadTaskListener() {
            @Override
            public void onComplete(final String url, final DownloadResponse downloadResponse) {
                if (downloadResponse == null || downloadResponse.getStatusCode() != HttpStatus.SC_OK) {
                    MoPubLog.d("Failed to hit tracking endpoint: " + url);
                    return;
                }

                String result = HttpResponses.asResponseString(downloadResponse);

                if (result != null) {
                    MoPubLog.d("Successfully hit tracking endpoint: " + url);
                } else {
                    MoPubLog.d("Failed to hit tracking endpoint: " + url);
                }
            }
        };

        final Runnable trackingHttpRequestRunnable = new Runnable() {
            @Override
            public void run() {
                for (final String url : urls) {
                    try {
                        final HttpGet httpGet = new HttpGet(url);
                        AsyncTasks.safeExecuteOnExecutor(new DownloadTask(downloadTaskListener), httpGet);
                    } catch (Exception e) {
                        MoPubLog.d("Failed to hit tracking endpoint: " + url);
                    }
                }
            }
        };

        new Handler(Looper.getMainLooper()).post(trackingHttpRequestRunnable);
    }

    public static void makeTrackingHttpRequest(final String url) {
        makeTrackingHttpRequest(Arrays.asList(url));
    }
}
