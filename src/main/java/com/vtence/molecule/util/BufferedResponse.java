package com.vtence.molecule.util;

import com.vtence.molecule.Body;
import com.vtence.molecule.Response;
import com.vtence.molecule.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class BufferedResponse extends ResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private Body body;

    public BufferedResponse(Response response) {
        super(response);
    }

    public void reset() throws IOException {
        buffer.reset();
    }

    public OutputStream outputStream() throws IOException {
        return buffer;
    }

    public OutputStream outputStream(int bufferSize) throws IOException {
        return buffer;
    }

    public void body(String text) throws IOException {
        body(new StringBody(text, charset()));
    }

    public void body(Body body) throws IOException {
        this.body = body;
        body.writeTo(outputStream(body.size()));
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

    public int size() {
        return buffer.size();
    }

    public boolean empty() {
        return size() == 0;
    }

    public void flush() throws IOException {
        super.outputStream(size()).write(content());
    }
}
