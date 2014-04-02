package com.vtence.molecule;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface Body extends Closeable {

    int size();

    void writeTo(OutputStream out) throws IOException;
}
