package com.vtence.molecule.lib;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;

public enum SecureProtocol {
    SSL("SSL"),
    TLS("TLS");

    private final String protocol;

    private SecureProtocol(String protocol) {
            this.protocol = protocol;
        }

    public SSLContext initialize(KeyManager[] keys) throws GeneralSecurityException {
        return initialize(keys, null);
    }

    public SSLContext initialize(KeyManager[] keys, TrustManager[] trusts) throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance(protocol);
        context.init(keys, trusts, null);
        return context;
    }
}