package com.vtence.molecule;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

// todo Consider letting implementations decide to implement Closeable
public interface Body extends Closeable {

    // see comment below
    int size();

    // todo Should we pass a charset as well? This would defer byte conversion of char responses
    // to the time the body is written to the response.
    void writeTo(OutputStream out) throws IOException;
}
