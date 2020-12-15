package com.vtence.molecule.lib;

import java.io.IOException;
import java.io.InputStream;

public class EmptyInputStream extends InputStream {
    public static final InputStream EMPTY = new EmptyInputStream();

    public int read() {
        return -1;
    }
}
