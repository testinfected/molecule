package com.vtence.molecule.session;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SerializerTest {

    Serializer<Session> serializer = new Serializer<>(Session.class);

    @Test public void
    roundTripsSession() throws Exception {
        Session data = new Session("42");
        data.put("username", "Edwin");
        data.put("race", "Human");

        byte[] encoded = serializer.marshall(data);

        Session decoded = serializer.unmarshall(encoded);

        assertThat("decoded session id", decoded.id(), equalTo(data.id()));
        for (String attribute : data.keys()) {
            assertThat("decoded " + attribute, decoded.get(attribute), equalTo(data.get(attribute)));
        }
    }
}