package com.vtence.molecule.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SecureSessionEncoder implements SessionEncoder {

    private static final byte[] NO_LINE_BREAK = new byte[0];

    private final Base64.Encoder encoder = Base64.getMimeEncoder(0, NO_LINE_BREAK);
    private final Base64.Decoder decoder = Base64.getMimeDecoder();

    private final List<String> keys = new ArrayList<>();
    private final Marshaller<Session> marshaller;
    private final DigestAlgorithm digester;

    public SecureSessionEncoder(String key) {
        this(key, new Serializer<>(Session.class), HMacDigest.SHA256());
    }

    public SecureSessionEncoder(String key, Marshaller<Session> marshaller, DigestAlgorithm digester) {
        this.keys.add(key);
        this.marshaller = marshaller;
        this.digester = digester;
    }

    public String encode(Session session) throws Exception {
        byte[] data = marshaller.marshall(session);
        byte[] digest = digester.compute(currentKey(), data);
        return toBase64(data) + "--" + toBase64(digest);
    }

    public Session decode(String content) throws Exception {
        String[] parts = content.split("--");
        byte[] data = fromBase64(parts[0]);
        byte[] digest = fromBase64(parts.length > 1 ? parts[1] : "");

        return digestsMatch(data, digest) ? marshaller.unmarshall(data) : null;
    }

    public SecureSessionEncoder acceptAlternateKeys(String... oldKeys) {
        keys.addAll(Arrays.asList(oldKeys));
        return this;
    }

    private String toBase64(byte[] data) {
        return encoder.encodeToString(data);
    }

    private byte[] fromBase64(String base64) {
        return decoder.decode(base64);
    }

    private String currentKey() {
        return keys.get(0);
    }

    private boolean digestsMatch(byte[] content, byte[] digest) throws Exception {
        for (String key : keys) {
            if (digester.verify(key, content, digest)) return true;
        }

        return false;
    }
}