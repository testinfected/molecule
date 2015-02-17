package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileUpload {

    private static String CRLF = "\r\n";

    private final String boundary = Long.toHexString(System.currentTimeMillis());;

    private final File file;
    private final String contentType;

    public FileUpload(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }

    public static FileUpload textFile(File file) {
        return new FileUpload(file, "text/plain");
    }

    public String boundary() {
        return boundary;
    }

    public String contentType() {
        return contentType;
    }

    public String encode(Charset charset) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("--").append(boundary).append(CRLF);
        content.append("Content-Disposition: form-data").append(CRLF);
        content.append("Content-Type: " + contentType + "; charset=").append(charset.name().toLowerCase()).append(CRLF);
        content.append(CRLF).append(readFileContent(charset)).append(CRLF);
        content.append("--").append(boundary).append("--").append(CRLF);
        return content.toString();
    }

    private String readFileContent(Charset charset) throws IOException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return Streams.toString(input, charset);
        } finally {
            Streams.close(input);
        }
    }
}
