package com.vtence.molecule.testing;

import com.vtence.molecule.Response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BodyContent {

    public static byte[] asBytes(Response response) {
        var out = new ByteArrayOutputStream();
        try {
            response.body().writeTo(out, response.charset());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return out.toByteArray();
    }

    public static String asText(Response response) {
        return new String(asBytes(response), response.charset());
    }

    public static InputStream asStream(Response response) {
        return new ByteArrayInputStream(asBytes(response));
    }

    private BodyContent() {}
}