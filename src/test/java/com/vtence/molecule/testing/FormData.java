package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FormData {

    private static String CRLF = "\r\n";

    private final List<TextField> entries = new ArrayList<TextField>();
    private final String boundary =  Long.toHexString(System.currentTimeMillis());
    private final String contentType;

    private Charset charset = Charsets.UTF_8;

    public FormData() {
        this("multipart/form-data");
    }

    public FormData(String contentType) {
        this.contentType = contentType;
    }

    public String contentType() {
        return contentType + "; boundary=" + boundary;
    }

    public FormData charset(String charsetName) {
        return charset(Charset.forName(charsetName));
    }

    public FormData charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public FormData add(String name, String value) {
        entries.add(new TextField(name, value));
        return this;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(buffer, charset);
        for (TextField entry : entries) {
            writer.append("--").append(boundary).append(CRLF);
            writer.flush();
            entry.encode(buffer, charset);
        }
        writer.append("--").append(boundary).append("--").append(CRLF);
        writer.flush();
        return buffer.toByteArray();
    }

    public static class TextField {
        public final String name;
        public final String value;

        public TextField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public void encode(OutputStream out, Charset charset) throws IOException {
            Writer writer = new OutputStreamWriter(out, charset);
            URLEscaper escaper = URLEscaper.to(charset);
            writer.append("Content-Disposition: form-data; name=\"").append(escaper.escape(name)).append("\"").append(CRLF);
            writer.append(CRLF);
            writer.append(value).append(CRLF);
            writer.flush();
        }
    }
}
