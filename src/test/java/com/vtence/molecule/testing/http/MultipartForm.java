package com.vtence.molecule.testing.http;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Streams;
import com.vtence.molecule.http.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MultipartForm extends Form {

    public static String CRLF = "\r\n";

    private final List<Entry> entries = new ArrayList<Entry>();
    private final String boundary =  Long.toHexString(System.currentTimeMillis());

    private Charset charset = Charsets.UTF_8;

    @Override
    public long contentLength() throws IOException {
        ByteCountingOutputStream out = new ByteCountingOutputStream();
        writeTo(out);
        return out.byteCount();
    }

    public String contentType() {
        return "multipart/form-data" + "; boundary=" + boundary;
    }

    public MultipartForm charset(String charsetName) {
        return charset(Charset.forName(charsetName));
    }

    public MultipartForm charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public MultipartForm addField(String name, String value) {
        entries.add(new TextField(name, value));
        return this;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        writeTo(buffer);
        return buffer.toByteArray();
    }

    public void writeTo(OutputStream out) throws IOException {
        Writer writer = new OutputStreamWriter(out, charset);
        for (Entry entry : entries) {
            writer.append("--").append(boundary).append(CRLF);
            writer.flush();
            entry.encode(out, charset);
        }
        writer.append("--").append(boundary).append("--").append(CRLF);
        writer.flush();
    }

    public MultipartForm addTextFile(String name, File file) {
        return addTextFile(name, file, guessMimeType(file));
    }

    public MultipartForm addTextFile(String name, File file, String contentType) {
        entries.add(new FileUpload(name, file, contentType, false));
        return this;
    }

    public MultipartForm addBinaryFile(String name, File toUpload) {
        return addBinaryFile(name, toUpload, guessMimeType(toUpload));
    }

    public MultipartForm addBinaryFile(String name, File toUpload, String contentType) {
        entries.add(new FileUpload(name, toUpload, contentType, true));
        return this;
    }

    private String guessMimeType(File file) {
        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public static interface Entry {
        void encode(OutputStream out, Charset charset) throws IOException;
    }

    static class FileUpload implements Entry {

        private final String name;
        private final File file;
        private final String contentType;
        private final boolean binary;

        public FileUpload(String name, File file, String contentType, boolean binary) {
            this.name = name;
            this.file = file;
            this.contentType = contentType;
            this.binary = binary;
        }

        public void encode(OutputStream out, Charset charset) throws IOException {
            Writer writer = new OutputStreamWriter(out, charset);
            URLEscaper escaper = URLEscaper.to(charset);
            writer.append("Content-Disposition: form-data");
            if (name != null) {
                writer.append("; name=\"").append(escaper.escape(name)).append("\"");
            }
            writer.append("; filename=\"").append(escaper.escape(file.getName())).append("\"").append(CRLF);
            writer.append("Content-Type: ").append(contentType);
            if (!binary) {
                writer.append("; charset=").append(fileCharset().name().toLowerCase()).append(CRLF);
            } else {
                writer.append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            }
            writer.append(CRLF);
            writer.flush();
            writeFileContent(out);
            writer.append(CRLF);
            writer.flush();
        }

        private void writeFileContent(OutputStream output) throws IOException {
            FileInputStream input = new FileInputStream(file);
            try {
                Streams.copy(input, output);
            } finally {
                Streams.close(input);
            }
        }

        private Charset fileCharset() {
            ContentType contentType = ContentType.parse(this.contentType);
            if (contentType == null || contentType.charset() == null) return Charsets.UTF_8;
            return contentType.charset();
        }
    }

    static class TextField implements Entry {
        public final String name;
        public final String value;

        public TextField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
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
