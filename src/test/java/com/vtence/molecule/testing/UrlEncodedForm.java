package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Joiner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class UrlEncodedForm extends Form {

    private final List<Field> fields = new ArrayList<Field>();
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

    public UrlEncodedForm addField(String name, String value) {
        fields.add(new Field(name, value));
        return this;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        return out.toByteArray();
    }

    public void writeTo(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, charset);
        List<String> pairs = new ArrayList<String>();
        for (Field field : fields) {
            pairs.add(field.encode(charset));
        }
        writer.write(Joiner.on("&").join(pairs));
        writer.flush();
    }

    static class Field {
        private final String name;
        private final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String encode(Charset charset) {
            URLEscaper escaper = URLEscaper.to(charset);
            return escaper.escape(name) + "=" + escaper.escape(value);
        }
    }
}
