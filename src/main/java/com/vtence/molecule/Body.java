package com.vtence.molecule;

import java.io.IOException;
import java.io.OutputStream;

public interface Body {

    int size();

    void writeTo(OutputStream outputStream) throws IOException;
}
