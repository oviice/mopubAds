package com.mopub.nativeads;

import android.graphics.Bitmap;

import com.mopub.nativeads.test.support.SdkTestRunner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.TestHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(SdkTestRunner.class)
public class ImageDownloadTaskManagerTest {

    private ImageDownloadTaskManager subject;
    private ImageDownloadTaskManager.ImageTaskManagerListener mMockImageTaskManagerListener;
    private Semaphore semaphore;
    private Map<String, Bitmap> networkImages;
    private FakeHttpLayer fakeHttpLayer;
    private String url1;
    private String url2;

    @Before
    public void setUp() throws Exception {
        semaphore = new Semaphore(0);
        mMockImageTaskManagerListener = mock(ImageDownloadTaskManager.ImageTaskManagerListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                Map<String, Bitmap> map = (Map)args[0];
                ImageDownloadTaskManagerTest.this.networkImages = map;
                semaphore.release();
                return null;
            }
        }).when(mMockImageTaskManagerListener).onSuccess(anyMap());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                semaphore.release();
                return null;
            }
        }).when(mMockImageTaskManagerListener).onFail();

        fakeHttpLayer = Robolectric.getFakeHttpLayer();
        url1 = "http://www.mopub.com/";
        url2 = "http://www.twitter.com";
    }

    @Test
    public void constructor_withValidUrlListAndListener_shouldReturnNewImageDownloadTaskManager() throws Exception {
        subject = new ImageDownloadTaskManager(
                Arrays.asList(url1, url2),
                mMockImageTaskManagerListener
        );
    }

    @Test
    public void constructor_withEmptyUrlListAndListener_shouldReturnNewImageDownloadTaskManager() throws Exception {
        subject = new ImageDownloadTaskManager(
                new ArrayList<String>(),
                mMockImageTaskManagerListener
        );
    }

    @Test
    public void constructor_withInvalidUrlList_shouldThrowIllegalArgumentException() throws Exception {
        try {
            subject = new ImageDownloadTaskManager(
                    Arrays.asList("BAD URL", url2),
                    mMockImageTaskManagerListener
            );
            fail("ImageDownloadTaskManager didn't throw an illegal argument exception");
        } catch (IllegalArgumentException e) {
            // pass
        }

        try {
            subject = new ImageDownloadTaskManager(
                    Arrays.asList(url1, null),
                    mMockImageTaskManagerListener
            );
            fail("ImageDownloadTaskManager didn't throw an illegal argument exception");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void constructor_withAnyNullParams_shouldThrowIllegalArgumentException() throws Exception {
        try {
            subject = new ImageDownloadTaskManager(
                    null,
                    mMockImageTaskManagerListener
            );
            fail("ImageDownloadTaskManager didn't throw an illegal argument exception");
        } catch (IllegalArgumentException e) {
            // pass
        }

        try {
            subject = new ImageDownloadTaskManager(
                    Arrays.asList(url1, url2),
                    null
            );
            fail("ImageDownloadTaskManager didn't throw an illegal argument exception");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    @Test
    public void execute_withValidUrlListAndListenerAndHttpResponses_shouldReturnMapOfUrlToBitmap() throws Exception {
        subject = new ImageDownloadTaskManager(
                Arrays.asList(url1, url2),
                mMockImageTaskManagerListener
        );

        String imageData1 = "image_data_1";
        Robolectric.addHttpResponseRule(
                url1,
                new TestHttpResponse(200, imageData1)
        );

        String imageData2 = "image_data_2";
        Robolectric.addHttpResponseRule(
                url2,
                new TestHttpResponse(200, imageData2)
        );

        subject.execute();
        semaphore.acquire();

        assertThat(networkImages.keySet()).containsOnly(url1, url2);

        Bitmap bitmap1 = networkImages.get(url1);
        assertThat(shadowOf(bitmap1).getCreatedFromBytes()).isEqualTo(imageData1.getBytes());

        Bitmap bitmap2 = networkImages.get(url2);
        assertThat(shadowOf(bitmap2).getCreatedFromBytes()).isEqualTo(imageData2.getBytes());

        verify(mMockImageTaskManagerListener).onSuccess(anyMap());
        verify(mMockImageTaskManagerListener, never()).onFail();
    }

    @Test
    public void execute_withEmptyUrlList_shouldReturnEmptyMap() throws Exception {
        subject = new ImageDownloadTaskManager(
                new ArrayList<String>(),
                mMockImageTaskManagerListener
        );

        subject.execute();
        semaphore.acquire();

        assertThat(networkImages.isEmpty()).isTrue();
        verify(mMockImageTaskManagerListener).onSuccess(anyMap());
        verify(mMockImageTaskManagerListener, never()).onFail();
    }

    @Test
    public void execute_withSingleNon200Response_shouldFailAllTasks() throws Exception {
        subject = new ImageDownloadTaskManager(
                Arrays.asList(url1, url1, url1, url1, url1),
                mMockImageTaskManagerListener
        );

        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(599, "");

        subject.execute();
        semaphore.acquire();

        verify(mMockImageTaskManagerListener, never()).onSuccess(anyMap());
        verify(mMockImageTaskManagerListener).onFail();
    }

    @Test
    public void execute_withMultipleNon200Response_shouldFailAllTasks() throws Exception {
        subject = new ImageDownloadTaskManager(
                Arrays.asList(url1, url1, url1, url1, url1),
                mMockImageTaskManagerListener
        );

        fakeHttpLayer.addPendingHttpResponse(599, "");
        fakeHttpLayer.addPendingHttpResponse(599, "");
        fakeHttpLayer.addPendingHttpResponse(599, "");
        fakeHttpLayer.addPendingHttpResponse(599, "");
        fakeHttpLayer.addPendingHttpResponse(599, "");

        subject.execute();
        semaphore.acquire();

        verify(mMockImageTaskManagerListener, never()).onSuccess(anyMap());
        verify(mMockImageTaskManagerListener).onFail();
    }

    @Ignore("need to fix concurrency logic")
    @Test
    public void execute_withSingleInvalidHttpResponse_shouldFailAllTasks() throws Exception {
        subject = new ImageDownloadTaskManager(
                Arrays.asList(url1, url1, url1, url1, url1),
                mMockImageTaskManagerListener
        );

        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(200, "");
        fakeHttpLayer.addPendingHttpResponse(createMockHttpResponseThatThrowsOnGetContent());

        subject.execute();
        semaphore.acquire();

        verify(mMockImageTaskManagerListener, never()).onSuccess(anyMap());
        verify(mMockImageTaskManagerListener).onFail();
    }

    private static HttpResponse createMockHttpResponseThatThrowsOnGetContent() throws IOException {
        HttpEntity mockHttpEntity = mock(HttpEntity.class);
        when(mockHttpEntity.getContent()).thenThrow(new IOException());

        TestHttpResponse mockHttpResponse = mock(TestHttpResponse.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockHttpResponse.new TestStatusLine());
        when(mockHttpResponse.getParams()).thenReturn(new BasicHttpParams());
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        return mockHttpResponse;
    }
}
