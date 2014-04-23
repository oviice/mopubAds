package com.mopub.nativeads;

import android.net.http.AndroidHttpClient;

import com.mopub.common.util.AsyncTasks;
import com.mopub.common.util.DeviceUtils;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import static com.mopub.nativeads.UrlResolutionTask.UrlResolutionListener;
import static com.mopub.nativeads.util.Utils.MoPubLog;

class NativeHttpClient {
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

    static void makeTrackingHttpRequest(final String url) {
        final DownloadTask httpDownloadTask = new DownloadTask(new DownloadTask.DownloadTaskListener() {
            @Override
            public void onComplete(final String url, final DownloadResponse downloadResponse) {
                if (downloadResponse == null || downloadResponse.getStatusCode() != HttpStatus.SC_OK) {
                    MoPubLog("Failed to hit tracking endpoint: " + url);
                    return;
                }

                String result = HttpResponses.asResponseString(downloadResponse);

                if (result != null) {
                    MoPubLog("Successfully hit tracking endpoint:" + url);
                } else {
                    MoPubLog("Failed to hit tracking endpoint: " + url);
                }
            }
        });

        try {
            final HttpGet httpGet = new HttpGet(url);
            httpDownloadTask.execute(httpGet);
        } catch (Exception e) {
            MoPubLog("Failed to hit tracking endpoint: " + url);
        }
    }

    static void getResolvedUrl(final String urlString, final UrlResolutionListener listener) {
        final UrlResolutionTask urlResolutionTask = new UrlResolutionTask(listener);
        AsyncTasks.safeExecuteOnExecutor(urlResolutionTask, urlString);
    }
}
