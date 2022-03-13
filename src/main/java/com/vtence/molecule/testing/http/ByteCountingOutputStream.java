package com.vtence.molecule.testing.http;

import java.io.IOException;
import java.io.OutputStream;

class ByteCountingOutputStream extends OutputStream {

    private long count;

    public long byteCount() {
        return count;
    }

    @Override
    public void write(int b) {
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        count += len;
    }
}