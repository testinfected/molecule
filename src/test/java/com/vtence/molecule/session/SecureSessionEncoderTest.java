package com.vtence.molecule.session;

import org.junit.Test;

import static com.vtence.molecule.session.SessionMatchers.sameSessionDataAs;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SecureSessionEncoderTest {

    String key = "secret";
    SecureSessionEncoder encoder = new SecureSessionEncoder(key);

    @Test public void
    decodesSessionWithIntegrityHash() throws Exception {
        Session data = new Session("42");
        data.put("username", "Joe");

        String secure = encoder.encode(data);
        Session decoded = encoder.decode(secure);

        assertThat("decoded session", decoded, sameSessionDataAs(data));
    }

    @Test public void
    ignoresTamperedWithEncodedContent() throws Exception {
        Session data = new Session("42");
        data.put("username", "Joe");

        String secure = encoder.encode(data);
        Session decoded = encoder.decode("tampered content" + digestPartOf(secure));
        assertThat("tampered session", decoded, nullValue());
    }

    @Test public void
    ignoresContentMissingIntegrityHash() throws Exception {
        Session data = new Session("42");
        data.put("username", "Joe");

        String secure = encoder.encode(data);
        Session decoded = encoder.decode(contentPartOf(secure));
        assertThat("tampered session", decoded, nullValue());
    }


    @Test public void
    allowsGracefulSecretRotation() throws Exception {
        Session data = new Session("42");
        data.put("username", "Joe");

        encoder = new SecureSessionEncoder("secret-1");
        String encoded = encoder.encode(data);

        encoder = new SecureSessionEncoder("secret-2").acceptAlternateKeys("secret-1");
        Session decoded = encoder.decode(encoded);
        assertThat("rotation #1", decoded, sameSessionDataAs(data));
        encoded = encoder.encode(data);

        encoder = new SecureSessionEncoder("secret-3").acceptAlternateKeys("secret-2");
        decoded = encoder.decode(encoded);
        assertThat("rotation #2", decoded, sameSessionDataAs(data));
    }

    private String contentPartOf(String secure) {
        String[] parts = secure.split("--");
        return parts[0];
    }

    private String digestPartOf(String secure) {
        String[] parts = secure.split("--");
        return parts[1];
    }
}
