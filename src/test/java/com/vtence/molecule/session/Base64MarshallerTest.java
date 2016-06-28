package com.vtence.molecule.session;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Base64MarshallerTest {

    String BASE64_WITHOUT_LINE_BREAKS =
            "([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";

    Base64Marshaller marshaller = new Base64Marshaller();

    @Test public void
    marshalsThenEncodesAsBase64WithoutLineBreaks() throws Exception {
        Session data = new Session("42");
        data.put("username", "Edwin");
        data.put("race", "Human");

        String encoded = marshaller.encode(data);

        assertThat("base64 without linebreaks?", encoded.matches(BASE64_WITHOUT_LINE_BREAKS), is(true));

        Session decoded = marshaller.decode(encoded);

        assertThat("decoded session id", decoded.id(), equalTo(data.id()));
        for (String attribute : data.keys()) {
            assertThat("decoded " + attribute, decoded.get(attribute), equalTo(data.get(attribute)));
        }
    }
}