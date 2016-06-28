package com.vtence.molecule.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecureSessionEncoder implements SessionEncoder {

    private final List<String> keys = new ArrayList<>();
    private final SessionEncoder marshaller;
    private final Digester digester;

    public SecureSessionEncoder(String key) {
        this(key, new Base64Marshaller(), new MacDigester());
    }

    public SecureSessionEncoder(String key, SessionEncoder marshaller, Digester digester) {
        this.keys.add(key);
        this.marshaller = marshaller;
        this.digester = digester;
    }

    public String encode(Session data) throws Exception {
        String encoded = marshaller.encode(data);
        return encoded + "--" + digester.computeDigest(currentKey(), encoded);
    }

    public Session decode(String content) throws Exception {
        String[] parts = content.split("--");
        String encoded = parts[0];
        String digest = parts.length > 1 ? parts[1] : null;

        return digestsMatch(encoded, digest) ? marshaller.decode(encoded) : null;
    }

    public SecureSessionEncoder acceptAlternateKeys(String... oldKeys) {
        keys.addAll(Arrays.asList(oldKeys));
        return this;
    }

    private String currentKey() {
        return keys.get(0);
    }

    private boolean digestsMatch(String encoded, String digest) throws Exception {
        if (digest == null) return false;

        for (String key : keys) {
            if (digester.checkDigest(key, encoded, digest)) return true;
        }

        return false;
    }
}