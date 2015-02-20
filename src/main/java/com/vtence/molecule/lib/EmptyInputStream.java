package com.vtence.molecule.lib;

import java.io.IOException;
import java.io.InputStream;

public class EmptyInputStream extends InputStream {
    public int read() throws IOException {
        return -1;
    }
}
