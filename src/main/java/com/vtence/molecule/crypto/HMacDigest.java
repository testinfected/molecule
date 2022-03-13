package com.vtence.molecule.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HMacDigest implements DigestAlgorithm {
    public static HMacDigest SHA256() {
        return new HMacDigest("HmacSHA256");
    }

    public HMacDigest(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] compute(String secret, byte[] content) throws Exception {
        var keySpec = new SecretKeySpec(encode(secret), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(keySpec);

        return mac.doFinal(content);
    }

    public boolean verify(String secret, byte[] content, byte[] digest) throws Exception {
        byte[] candidate = compute(secret, content);
        return slowEquals(candidate, digest);
    }

    private final String algorithm;

    private byte[] encode(String key) {
        return key.getBytes(UTF_8);
    }

    // Compare arrays in constant time to avoid timing attacks
    private boolean slowEquals(byte[] bytes, byte[] other) {
        int diff = bytes.length ^ other.length;
        for(int i = 0; i < bytes.length && i < other.length; i++)
            diff |= bytes[i] ^ other[i];
        return diff == 0;
    }
}
