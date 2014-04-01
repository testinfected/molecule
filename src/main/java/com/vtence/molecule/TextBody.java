package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class TextBody implements Body {

    private final StringBuilder body = new StringBuilder();
    private final Charset charset;

    public static TextBody text(String text, Charset charset) {
        TextBody body = new TextBody(charset);
        body.append(text);
        return body;
    }

    public TextBody(Charset charset) {
        this.charset = charset;
    }

    public void append(String text) {
        body.append(text);
    }

    public int size() {
        return content().length;
    }

    private byte[] content() {
        return body.toString().getBytes(charset);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(content());
    }
}
