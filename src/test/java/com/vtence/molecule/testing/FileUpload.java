package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Streams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class FileUpload {

    private static String CRLF = "\r\n";

    private final File file;
    private final String contentType;
    private boolean binary;
    private Charset charset = Charset.defaultCharset();

    public FileUpload(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }

    public static FileUpload textFile(File file) {
        return new FileUpload(file, "text/plain");
    }

    public static FileUpload binaryFile(File file) {
        return new FileUpload(file, contentTypeOf(file)).binary(true);
    }

    private static String contentTypeOf(File file) {
        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public FileUpload encodedAs(Charset charset) {
        this.charset = charset;
        return this;
    }

    public FileUpload binary(boolean binary) {
        this.binary = binary;
        return this;
    }

    public byte[] encode(String boundary) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // MIME text is always ASCII, so we can safely use the file encoding
        Writer writer = new OutputStreamWriter(buffer, charset);

        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data");
        writer.append("; filename=\"").append(file.getName()).append("\"").append(CRLF);
        writer.append("Content-Type: ").append(contentType);
        if (!binary) {
            writer.append("; charset=").append(charset.name().toLowerCase()).append(CRLF);
        } else {
            writer.append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        }
        writer.append(CRLF);
        writer.flush();
        writeFileContent(buffer);
        writer.append(CRLF);
        writer.append("--").append(boundary).append("--").append(CRLF);
        writer.flush();
        return buffer.toByteArray();
    }

    private void writeFileContent(OutputStream output) throws IOException {
        FileInputStream input = new FileInputStream(file);
        try {
            Streams.copy(input, output);
        } finally {
            Streams.close(input);
        }
    }
}
