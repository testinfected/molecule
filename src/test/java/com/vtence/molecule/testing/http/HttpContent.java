package com.vtence.molecule.testing.http;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpContent {

    long contentLength() throws IOException;

    String contentType();

    void writeTo(OutputStream out) throws IOException;
}