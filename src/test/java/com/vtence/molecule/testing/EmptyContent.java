package com.vtence.molecule.testing;

import java.io.IOException;
import java.io.OutputStream;

public class EmptyContent implements HttpContent {

    @Override
    public long contentLength() throws IOException {
        return 0;
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
    }
}
