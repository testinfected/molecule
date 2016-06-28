package com.vtence.molecule.session;

import com.vtence.molecule.helpers.HexEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MacDigester implements Digester {

    public static final String HMAC_SHA1 = "HmacSHA1";

    private final String algorithm;
    private final Charset charset = StandardCharsets.UTF_8;
    private final HexEncoder hex = new HexEncoder();

    public MacDigester() {
        this(HMAC_SHA1);
    }

    public MacDigester(String algorithm) {
        this.algorithm = algorithm;
    }

    public String computeDigest(String key, String content) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encode(key), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(keySpec);

        byte[] digest = mac.doFinal(encode(content));
        return hex.toHex(digest);
    }

    public boolean checkDigest(String key, String content, String digest) throws Exception {
        String candidate = computeDigest(key, content);
        return slowEquals(encode(candidate), encode(digest));
    }

    private byte[] encode(String key) throws UnsupportedEncodingException {
        return key.getBytes(charset);
    }

    private boolean slowEquals(byte[] bytes, byte[] other) {
        // Compare arrays in constant time
        int diff = bytes.length ^ other.length;
        for(int i = 0; i < bytes.length && i < other.length; i++)
            diff |= bytes[i] ^ other[i];
        return diff == 0;
    }
}
