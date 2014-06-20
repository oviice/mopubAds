package com.mopub.common;

import com.mopub.common.util.ResponseHeader;
import com.mopub.mobileads.test.support.TestHttpResponseWithHeaders;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.util.concurrent.Semaphore;

import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class DownloadTaskTest {

    private DownloadTask mDownloadTask;
    private DownloadTask.DownloadTaskListener mDownloadTaskListener;
    private Semaphore mSemaphore;
    private String mUrl;
    private DownloadResponse mDownloadResponse;
    private HttpGet httpGet;
    private String mTestResponse;
    private FakeHttpLayer mFakeHttpLayer;
    private TestHttpResponseWithHeaders mTestHttpResponseWithHeaders;

    @Before
    public void setUp() throws Exception {
        mSemaphore = new Semaphore(0);

        mDownloadTaskListener = new DownloadTask.DownloadTaskListener() {
            @Override
            public void onComplete(String url, DownloadResponse response) {
                mUrl = url;
                mDownloadResponse = response;
                mSemaphore.release();
            }
        };
        mDownloadTask = spy(new DownloadTask(mDownloadTaskListener));
        try {
            httpGet = new HttpGet("http://www.mopub.com/");
        } catch (IllegalArgumentException e) {
            fail("Could not initialize HttpGet in test");
        }

        mTestResponse = "TEST RESPONSE";
        mTestHttpResponseWithHeaders = new TestHttpResponseWithHeaders(200, mTestResponse);
        mTestHttpResponseWithHeaders.addHeader(ResponseHeader.IMPRESSION_URL.getKey(), "moPubImpressionTrackerUrl");
        mTestHttpResponseWithHeaders.addHeader(ResponseHeader.CLICKTHROUGH_URL.getKey(), "moPubClickTrackerUrl");

        mFakeHttpLayer = Robolectric.getFakeHttpLayer();
    }

    @Test
    public void execute_whenDownloadTaskAndHttpClientCompleteSuccessfully_shouldReturn200HttpResponse() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(mTestHttpResponseWithHeaders);
        mDownloadTask.execute(httpGet);
        mSemaphore.acquire();
        assertThat(mUrl).isEqualTo(httpGet.getURI().toString());
        assertThat(mDownloadResponse.getStatusCode()).isEqualTo(200);
        assertThat(mDownloadResponse.getFirstHeader(ResponseHeader.IMPRESSION_URL)).isEqualTo("moPubImpressionTrackerUrl");
        assertThat(mDownloadResponse.getFirstHeader(ResponseHeader.CLICKTHROUGH_URL)).isEqualTo("moPubClickTrackerUrl");
        assertThat(HttpResponses.asResponseString(mDownloadResponse)).isEqualTo(mTestResponse);
    }

    @Test
    public void execute_whenDownloadTaskCompletesSuccessfullyAndHttpClientTimesOut_shouldReturn599HttpResponse() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(599, "");
        mDownloadTask.execute(httpGet);
        mSemaphore.acquire();
        assertThat(mUrl).isEqualTo(httpGet.getURI().toString());
        assertThat(mDownloadResponse.getStatusCode()).isEqualTo(599);
        assertThat(HttpResponses.asResponseString(mDownloadResponse)).isEqualTo("");
    }

    @Test
    public void execute_whenDownloadTaskIsCancelledBeforeExecute_shouldReturnNullHttpReponseAndNullUrl() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(200, mTestResponse);
        mDownloadTask.cancel(true);
        mDownloadTask.execute(httpGet);
        mSemaphore.acquire();
        assertThat(mUrl).isEqualTo(null);
        assertThat(mDownloadResponse).isEqualTo(null);
    }

    @Ignore("pending")
    @Test
    public void execute_whenDownloadTaskIsCancelledDuringDoInBackground_shouldReturnNullHttpReponse() throws Exception {
        // need a way to reliably cancel task during doInBackground
    }

    @Ignore("pending")
    @Test
    public void execute_whenHttpUriRequestThrowsIOException_shouldCancelTaskAndReturnNullHttpResponse() throws Exception {
        // need a way to force HttpUriRequest to throw on execute
    }

    @Test
    public void execute_whenHttpUriRequestIsNull_shouldReturnNullHttpReponseAndNullUrl() throws Exception {
        mDownloadTask.execute((HttpUriRequest) null);
        mSemaphore.acquire();
        assertThat(mUrl).isEqualTo(null);
        assertThat(mDownloadResponse).isEqualTo(null);
    }

    @Test
    public void execute_whenHttpUriRequestIsNullArray_shouldReturnNullHttpReponseAndNullUrl() throws Exception {
        mDownloadTask.execute((HttpUriRequest[])null);
        mSemaphore.acquire();
        assertThat(mUrl).isEqualTo(null);
        assertThat(mDownloadResponse).isEqualTo(null);
    }

    @Test
    public void execute_whenHttpUriRequestIsArray_shouldOnlyReturnFirstResponse() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(200, mTestResponse);
        mFakeHttpLayer.addPendingHttpResponse(500, "");
        mDownloadTask.execute(httpGet, new HttpGet("http://www.twitter.com/"));
        mSemaphore.acquire();
        assertThat(mDownloadResponse.getStatusCode()).isEqualTo(200);
        assertThat(HttpResponses.asResponseString(mDownloadResponse)).isEqualTo(mTestResponse);
    }

    @Test
    public void downLoadTask_whenConstructedWithNullListener_shouldThrowIllegalArgumentException() throws Exception {
        try {
            new DownloadTask(null);
            fail("DownloadTask didn't throw IllegalArgumentException when constructed with null");
        } catch (IllegalArgumentException e) {
            // passed
        }
    }
}
