package com.vtence.molecule.http;

import com.vtence.molecule.lib.MimeDecoder;

public class BasicCredentials {
    private static final MimeDecoder decoder = MimeDecoder.fromUtf8();

    private final String[] credentials;

    public BasicCredentials(String... credentials) {
        this.credentials = credentials;
    }

    public static BasicCredentials decode(String params) {
        return new BasicCredentials(unpack(params));
    }

    private static String[] unpack(String params) {
        return decoder.decode(params).split(":");
    }

    public String username() {
        return credentials.length > 0 ? credentials[0] : "";
    }

    public String password() {
        return credentials.length > 1 ? credentials[1] : "";
    }
}
