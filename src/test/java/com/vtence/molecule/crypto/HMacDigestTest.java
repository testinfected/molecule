package com.vtence.molecule.crypto;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class HMacDigestTest {

    HMacDigest hmac = HMacDigest.SHA256();
    String secret = "secret";

    @Test
    public void
    computesDigestBasedOnSecretKey() throws Exception {
        byte[] digest = hmac.compute(secret, b("content"));

        assertThat("digest", s(digest), not(equalTo("content")));
        assertThat("valid digest", hmac.verify(secret, b("content"), digest), is(true));
        assertThat("tampered digest", hmac.verify(secret, b("content"), b("tampered")), is(false));
        assertThat("different content", hmac.verify(secret, b("other content"), digest), is(false));
    }

    private byte[] b(String s) {
        return s.getBytes(UTF_8);
    }

    private String s(byte[] b) {
        return new String(b, UTF_8);
    }
}
