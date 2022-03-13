package com.vtence.molecule.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public enum KeyStoreType {
    DEFAULT(KeyStore.getDefaultType(), KeyManagerFactory.getDefaultAlgorithm()),
    JKS("JKS", "SunX509"),
    PKCS12("PKCS12", "SunX509");

    private final String type;
    private final String algorithm;

    KeyStoreType(String type, String algorithm) {
        this.type = type;
        this.algorithm = algorithm;
    }

    public KeyStore open(InputStream keyStore, String password) throws GeneralSecurityException, IOException {
        var store = KeyStore.getInstance(type);
        store.load(keyStore, password.toCharArray());
        return store;
    }

    public KeyStore open(File keyStore, String password) throws GeneralSecurityException, IOException {
        try (var source = new FileInputStream(keyStore)) {
            return open(source, password);
        }
    }

    public KeyManager[] loadKeys(KeyStore keyStore, String keyPassword) throws GeneralSecurityException {
        var keys = KeyManagerFactory.getInstance(algorithm);
        keys.init(keyStore, keyPassword.toCharArray());
        return keys.getKeyManagers();
    }

    public KeyManager[] loadKeys(File keyStore, String storePassword, String keyPassword) throws GeneralSecurityException, IOException {
        return loadKeys(open(keyStore, storePassword), keyPassword);
    }
}