package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class TextBody implements Body {

    private final StringBuilder content = new StringBuilder();
    private final Charset charset;

    public static TextBody text(String text, Charset charset) {
        return new TextBody(charset).append(text);
    }

    public TextBody(Charset charset) {
        this.charset = charset;
    }

    public TextBody append(CharSequence text) {
        this.content.append(text);
        return this;
    }

    public String text() {
        return content.toString();
    }

    public long size() {
        return content().length;
    }

    private byte[] content() {
        return text().getBytes(charset);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(content());
    }

    public void close() throws IOException {
    }
}
