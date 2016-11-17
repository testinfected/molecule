package com.vtence.molecule.testing.http;

import com.vtence.molecule.http.ContentType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextContent implements HttpContent {

    private final String text;
    private final String contentType;

    public TextContent(String text, String contentType) {
        this.text = text;
        this.contentType = contentType;
    }

    @Override
    public long contentLength() {
        return content().length;
    }

    public String contentType() {
        return contentType;
    }

    private byte[] content() {
        return text.getBytes(charset());
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(content());
    }

    private Charset charset() {
        ContentType contentType = ContentType.parse(contentType());
        if (contentType == null || contentType.charset() == null) return StandardCharsets.ISO_8859_1;

        return contentType.charset();
    }
}
