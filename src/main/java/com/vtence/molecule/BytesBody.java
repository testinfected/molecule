package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;

public class BytesBody implements Body {

    private static final Body EMPTY = bytes(new byte[0]);

    private final byte[] content;

    public static Body empty() {
        return EMPTY;
    }

    public static Body bytes(byte[] content) {
        return new BytesBody(content);
    }

    public BytesBody(byte[] content) {
        this.content = content;
    }

    public long size() {
        return content.length;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(content);
    }

    public void close() throws IOException {
    }
}
