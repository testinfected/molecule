package com.vtence.molecule.testing.http;

import com.vtence.molecule.helpers.Charsets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;

public class UrlEncodedForm extends Form {

    private Charset charset = Charsets.UTF_8;

    @Override
    public long contentLength() throws IOException {
        ByteCountingOutputStream out = new ByteCountingOutputStream();
        writeTo(out);
        return out.byteCount();
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

    public UrlEncodedForm addField(Field field) {
        super.addField(field);
        return this;
    }

    public UrlEncodedForm addField(String name, String value) {
        return addField(new TextField(name, value));
    }

    public void writeTo(OutputStream out) throws IOException {
        Iterator<Field> fields = this.fields.iterator();
        if (!fields.hasNext()) return;

        Field first = fields.next();
        first.encode(out, charset);

        while (fields.hasNext()) {
            // this is safe
            out.write("&".getBytes());
            fields.next().encode(out, charset);
        }
    }

     static class TextField implements Field {
        private final String name;
        private final String value;

        public TextField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void encode(OutputStream out, Charset charset) throws IOException {
            URLEscaper escaper = URLEscaper.to(charset);
            Writer writer = new OutputStreamWriter(out, charset);
            writer.append(escaper.escape(name)).append("=").append(escaper.escape(value));
            writer.flush();
        }
    }
}
