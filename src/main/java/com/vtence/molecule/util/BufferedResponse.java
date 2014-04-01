package com.vtence.molecule.util;

import com.vtence.molecule.Body;
import com.vtence.molecule.Response;
import com.vtence.molecule.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class BufferedResponse extends ResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

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

    public Writer writer() throws IOException {
        return new OutputStreamWriter(outputStream(), charset());
    }

    public void body(String text) throws IOException {
        body(StringBody.text(text, charset()));
    }

    public void body(Body body) throws IOException {
        body.writeTo(outputStream());
    }

    public String body() throws UnsupportedEncodingException {
        return new String(content(), charset());
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
