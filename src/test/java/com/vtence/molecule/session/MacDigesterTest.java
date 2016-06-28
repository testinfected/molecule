package com.vtence.molecule.session;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class MacDigesterTest {

    MacDigester digester = new MacDigester();
    String key = "secret";

    @Test
    public void
    computesDigestBasedOnSecretKey() throws Exception {
        String digest = digester.computeDigest(key, "content");

        assertThat("digest value", digest, not(equalTo("content")));
        assertThat("valid hash", digester.checkDigest(key, "content", digest), is(true));
        assertThat("tampered hash", digester.checkDigest(key, "content", "tampered"), is(false));
        assertThat("different content", digester.checkDigest(key, "other content", digest), is(false));
    }
}
