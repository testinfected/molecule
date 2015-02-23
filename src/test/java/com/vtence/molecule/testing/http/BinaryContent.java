package com.vtence.molecule.testing.http;

import java.io.IOException;
import java.io.OutputStream;

public class BinaryContent implements HttpContent {
    private final byte[] content;
    private final String contentType;

    public BinaryContent(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public long contentLength() {
        return content.length;
    }

    public String contentType() {
        return contentType;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(content);
    }
}
