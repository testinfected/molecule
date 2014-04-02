package com.vtence.molecule.util;

import com.vtence.molecule.BinaryBody;
import com.vtence.molecule.Body;
import com.vtence.molecule.Response;
import com.vtence.molecule.TextBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BufferedResponse extends ResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private Body body = BinaryBody.empty();

    public BufferedResponse(Response response) {
        super(response);
    }

    public void reset() throws IOException {
        buffer.reset();
    }

    public void body(String text) throws IOException {
        body(TextBody.text(text, charset()));
    }

    public void body(Body body) throws IOException {
        this.body = body;
        body.writeTo(buffer);
    }

    public Body body() {
        return body;
    }

    public String text() throws UnsupportedEncodingException {
        return buffer.toString(charset().name());
    }

    public byte[] content() {
        return buffer.toByteArray();
    }

    public long size() {
        return buffer.size();
    }

    public boolean empty() {
        return size() == 0;
    }
}
