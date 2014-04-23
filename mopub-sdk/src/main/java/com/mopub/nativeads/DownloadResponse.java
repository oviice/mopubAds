package com.mopub.nativeads;

import com.mopub.common.util.Streams;

import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

class DownloadResponse {
    private byte[] bytes = new byte[0];
    private final int statusCode;
    private final long contentLength;

    DownloadResponse(final HttpResponse httpResponse) throws Exception {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
            Streams.copyContent(inputStream, outputStream);
            bytes = outputStream.toByteArray();
        } finally {
            Streams.closeStream(inputStream);
            Streams.closeStream(outputStream);
        }

        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.contentLength = httpResponse.getEntity().getContentLength();
    }

    byte[] getByteArray() {
        return bytes;
    }

    int getStatusCode() {
        return statusCode;
    }

    long getContentLength() {
        return contentLength;
    }
}
