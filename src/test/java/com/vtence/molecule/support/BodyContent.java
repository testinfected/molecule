package com.vtence.molecule.support;

import com.vtence.molecule.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BodyContent {

    public static byte[] of(Response response) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            response.body().writeTo(out, response.charset());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return out.toByteArray();
    }

    public static String asText(Response response) {
        return new String(BodyContent.of(response), response.charset());
    }

    private BodyContent() {}
}
