package com.vtence.molecule.testing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class URLEscaper {

    private final Charset charset;

    public URLEscaper(Charset charset) {
        this.charset = charset;
    }

    public static URLEscaper to(Charset charset) {
        return new URLEscaper(charset);
    }

    public String escape(String text) {
        try {
            return URLEncoder.encode(text, charset.name());
        } catch (UnsupportedEncodingException impossible) {
            // We can safely ignore since we already have a charset
            throw new AssertionError(impossible);
        }
    }
}