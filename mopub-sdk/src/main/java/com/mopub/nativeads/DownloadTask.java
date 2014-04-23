package com.mopub.nativeads;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import static com.mopub.nativeads.util.Utils.MoPubLog;

class DownloadTask extends AsyncTask<HttpUriRequest, Void, DownloadResponse> {
    private final DownloadTaskListener mDownloadTaskListener;
    private String mUrl;

    interface DownloadTaskListener {
        abstract void onComplete(String url, DownloadResponse downloadResponse);
    }

    DownloadTask(final DownloadTaskListener downloadTaskListener) throws IllegalArgumentException {
        if (downloadTaskListener == null) {
            throw new IllegalArgumentException("DownloadTaskListener must not be null.");
        }

        mDownloadTaskListener = downloadTaskListener;
    }

    @Override
    protected DownloadResponse doInBackground(final HttpUriRequest... httpUriRequests) {
        if (httpUriRequests == null || httpUriRequests.length == 0 || httpUriRequests[0] == null) {
            MoPubLog("Download task tried to execute null or empty url");
            return null;
        }

        final HttpUriRequest httpUriRequest = httpUriRequests[0];
        mUrl = httpUriRequest.getURI().toString();

        final AndroidHttpClient httpClient = NativeHttpClient.getHttpClient();
        try {
            final HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            return new DownloadResponse(httpResponse);
        } catch (Exception e) {
            MoPubLog("Download task threw an internal exception");
            cancel(true);
            return null;
        } finally {
            httpClient.close();
        }
    }

    @Override
    protected void onPostExecute(final DownloadResponse downloadResponse) {
        if (isCancelled()) {
            onCancelled();
            return;
        }

        mDownloadTaskListener.onComplete(mUrl, downloadResponse);
    }

    @Override
    protected void onCancelled() {
        mDownloadTaskListener.onComplete(mUrl, null);
    }
}
