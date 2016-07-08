package com.vtence.molecule.lib;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MimeEncoder {

    private final Base64.Encoder encoder = Base64.getMimeEncoder();
    private final Charset charset;

    public MimeEncoder(Charset charset) {
        this.charset = charset;
    }

    public static MimeEncoder inUtf8() {
        return new MimeEncoder(StandardCharsets.UTF_8);
    }

    public String encode(String src) {
        return encoder.encodeToString(src.getBytes(charset));
    }
}
