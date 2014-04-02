package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class StringBody implements Body {

    private final String text;
    private final Charset charset;

    public StringBody(String text, Charset charset) {
        this.text = text;
        this.charset = charset;
    }

    public long size() {
        return content().length;
    }

    private byte[] content() {
        return text.getBytes(charset);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(content());
    }

    public void close() throws IOException {
    }
}
