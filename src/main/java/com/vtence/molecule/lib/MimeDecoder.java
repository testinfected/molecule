package com.vtence.molecule.lib;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MimeDecoder {

    private final Base64.Decoder decoder = Base64.getMimeDecoder();
    private final Charset charset;

    public MimeDecoder(Charset charset) {
        this.charset = charset;
    }

    public static MimeDecoder fromUtf8() {
        return new MimeDecoder(StandardCharsets.UTF_8);
    }

    public String decode(String src) {
        return new String(decoder.decode(src.getBytes()), charset);
    }
}