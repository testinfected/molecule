package com.vtence.molecule.testing;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class FormData {

    private static String CRLF = "\r\n";

    private final String boundary;
    private final Map<String, String> data = new HashMap<String, String>();

    public FormData() {
        this(makeBoundary());
    }

    private static String makeBoundary() {
        return Long.toHexString(System.currentTimeMillis());
    }

    public FormData(String boundary) {
        this.boundary = boundary;
    }

    public String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    public FormData set(String name, String value) {
        data.put(name, value);
        return this;
    }

    public String encode(Charset charset) {
        StringBuilder content = new StringBuilder();
        for (String param : data.keySet()) {
            content.append("--").append(boundary).append(CRLF);
            content.append("Content-Disposition: form-data; name=\"").append(param).append("\"").append(CRLF);
            content.append("Content-Type: text/plain; charset=").append(charset.name().toLowerCase()).append(CRLF);
            content.append(CRLF).append(data.get(param)).append(CRLF);
        }
        content.append("--").append(boundary).append("--").append(CRLF);
        return content.toString();
    }
}
