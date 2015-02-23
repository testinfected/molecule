package com.vtence.molecule.testing;

import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetector {

    public static String detectCharsetOf(byte[] data) {
        UniversalDetector charsetDetector = new UniversalDetector(new CharsetListener() {
            public void report(String charset) {
            }
        });
        charsetDetector.handleData(data, 0, data.length);
        charsetDetector.dataEnd();
        return charsetDetector.getDetectedCharset();
    }

    private CharsetDetector() {}
}