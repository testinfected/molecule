package com.vtence.molecule.testing;

import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetector {

    public static String detectCharsetOf(byte[] data) {
        var charsetDetector = new UniversalDetector(charset -> {});
        charsetDetector.handleData(data, 0, data.length);
        charsetDetector.dataEnd();
        return charsetDetector.getDetectedCharset();
    }

    private CharsetDetector() {}
}