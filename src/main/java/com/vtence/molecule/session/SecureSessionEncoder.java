package com.vtence.molecule.session;

public class SecureSessionEncoder implements SessionEncoder {

    private final String key;
    private final SessionEncoder marshaller;
    private final Digester digester;

    public SecureSessionEncoder(String key) {
        this(key, new Base64Marshaller(), new MacDigester());
    }

    public SecureSessionEncoder(String key, SessionEncoder marshaller, Digester digester) {
        this.key = key;
        this.marshaller = marshaller;
        this.digester = digester;
    }

    public String encode(Session data) throws Exception {
        String encoded = marshaller.encode(data);
        return encoded + "--" + digester.computeDigest(key, encoded);
    }

    public Session decode(String content) throws Exception {
        String[] parts = content.split("--");
        String encoded = parts[0];
        String digest = parts.length > 1 ? parts[1] : null;

        return digestsMatch(encoded, digest) ? marshaller.decode(encoded) : null;
    }

    private boolean digestsMatch(String encoded, String digest) throws Exception {
        return digest != null && digester.checkDigest(key, encoded, digest);
    }
}