package com.vtence.molecule.testing.http;

import com.vtence.molecule.http.ContentType;

import java.io.*;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Flow;

public class MultipartForm extends Form {

    public static String CRLF = "\r\n";

    private final String boundary =  Long.toHexString(System.currentTimeMillis());

    private Charset charset = StandardCharsets.UTF_8;

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        var buffer = new ByteArrayOutputStream();
        try {
            writeTo(buffer);
            var delegate = HttpRequest.BodyPublishers.ofByteArray(buffer.toByteArray());
            delegate.subscribe(subscriber);
        } catch (IOException e) {
            subscriber.onError(e);
        }
    }

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
        addPart(new Field(name, value));
        return this;
    }

    public MultipartForm addTextFile(String name, File file) throws IOException {
        return addTextFile(name, file, guessMimeType(file));
    }

    public MultipartForm addTextFile(String name, File file, String contentType) {
        addPart(new FilePart(name, file, contentType, false));
        return this;
    }

    public MultipartForm addBinaryFile(String name, File file) throws IOException {
        return addBinaryFile(name, file, guessMimeType(file));
    }

    public MultipartForm addBinaryFile(String name, File file, String contentType) {
        addPart(new FilePart(name, file, contentType, true));
        return this;
    }

    public void writeTo(OutputStream out) throws IOException {
        var writer = new OutputStreamWriter(out, charset);
        for (Part part : parts) {
            writer.append("--").append(boundary).append(CRLF);
            writer.flush();
            part.encode(out, charset);
        }
        writer.append("--").append(boundary).append("--").append(CRLF);
        writer.flush();
    }

    private String guessMimeType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }

    static class Field implements Part {

        public final String name;
        public final String value;

        public Field(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void encode(OutputStream out, Charset charset) throws IOException {
            var writer = new OutputStreamWriter(out, charset);
            var escaper = URLEscaper.to(charset);
            writer.append("Content-Disposition: form-data; name=\"").append(escaper.escape(name)).append("\"").append(CRLF);
            writer.append(CRLF);
            writer.append(value).append(CRLF);
            writer.flush();
        }
    }

    static class FilePart implements Part {

        private final String name;
        private final File file;
        private final String contentType;
        private final boolean binary;

        public FilePart(String name, File file, String contentType, boolean binary) {
            this.name = name;
            this.file = file;
            this.contentType = contentType;
            this.binary = binary;
        }

        public void encode(OutputStream out, Charset charset) throws IOException {
            var writer = new OutputStreamWriter(out, charset);
            var escaper = URLEscaper.to(charset);
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
            try (FileInputStream input = new FileInputStream(file)) {
                input.transferTo(output);
            }
        }

        private Charset fileCharset() {
            var contentType = ContentType.parse(this.contentType);
            return contentType.charset() != null ? contentType.charset() : StandardCharsets.UTF_8;
        }
    }
}
