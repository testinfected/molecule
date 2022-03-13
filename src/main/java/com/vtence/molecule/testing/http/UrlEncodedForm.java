package com.vtence.molecule.testing.http;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.Flow;

import static java.net.http.HttpRequest.BodyPublisher;
import static java.net.http.HttpRequest.BodyPublishers;

public class UrlEncodedForm extends Form {

    private Charset charset = StandardCharsets.UTF_8;

    @Override
    public long contentLength() {
        var out = new ByteCountingOutputStream();
        try {
            writeTo(out);
        } catch (IOException ignored) {
            return -1;
        }
        return out.byteCount();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        var buffer = new ByteArrayOutputStream();
        try {
            writeTo(buffer);
            var delegate = BodyPublishers.ofByteArray(buffer.toByteArray());
            delegate.subscribe(subscriber);
        } catch (IOException e) {
            subscriber.onError(e);
        }
    }

    @Override
    public String contentType() {
        return "application/x-www-form-urlencoded";
    }

    public UrlEncodedForm charset(String charsetName) {
        return charset(Charset.forName(charsetName));
    }

    public UrlEncodedForm charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public UrlEncodedForm addField(String name, String value) {
        addPart(new Field(name, value));
        return this;
    }

    private void writeTo(OutputStream out) throws IOException {
        Iterator<Part> parts = this.parts.iterator();
        if (!parts.hasNext()) return;

        Part first = parts.next();
        first.encode(out, charset);

        while (parts.hasNext()) {
            // this is safe
            out.write("&".getBytes());
            parts.next().encode(out, charset);
        }
    }

     private static class Field implements Part {
        private final String name;
        private final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public void encode(OutputStream out, Charset charset) throws IOException {
            var escaper = URLEscaper.to(charset);
            var writer = new OutputStreamWriter(out, charset);
            writer.append(escaper.escape(name)).append("=").append(escaper.escape(value));
            writer.flush();
        }
    }
}
