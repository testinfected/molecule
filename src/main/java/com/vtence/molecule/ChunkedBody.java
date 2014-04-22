package com.vtence.molecule;

import java.nio.charset.Charset;

public abstract class ChunkedBody implements Body {
    public long size(Charset charset) {
        return -1;
    }
}
