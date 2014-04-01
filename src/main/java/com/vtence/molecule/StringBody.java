package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StringBody implements Body {

    private final StringBuilder body = new StringBuilder();
    private final Charset charset;

    public static StringBody text(String text, Charset charset) {
        StringBody body = new StringBody(charset);
        body.append(text);
        return body;
    }

    public StringBody(Charset charset) {
        this.charset = charset;
    }

    public void append(String text) {
        body.append(text);
    }

    public long size() {
        return content().length;
    }

    private byte[] content() {
        return body.toString().getBytes(charset);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(content());
    }

    public void close() throws IOException {
    }
}
