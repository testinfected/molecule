package com.vtence.molecule.testing;

import com.vtence.molecule.helpers.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class FormData {

    private static String CRLF = "\r\n";

    private final Map<String, String> data = new HashMap<String, String>();
    private final String contentType;
    private final Charset charset = Charsets.UTF_8;

    public FormData() {
        this("text/plain");
    }

    public FormData(String contentType) {
        this.contentType = contentType;
    }

    public FormData set(String name, String value) {
        data.put(name, value);
        return this;
    }

    public byte[] encode(String boundary) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(buffer, charset);
        for (String param : data.keySet()) {
            writer.append("--").append(boundary).append(CRLF)
                  .append("Content-Disposition: form-data; name=\"").append(param).append("\"").append(CRLF);

            if (contentType != null) {
                writer.append("Content-Type: ").append(contentType)
                      .append("; charset=").append(charset.name().toLowerCase()).append(CRLF);
            }

            writer.append(CRLF);
            writer.append(data.get(param)).append(CRLF);
        }

        writer.append("--").append(boundary).append("--").append(CRLF)
              .flush();
        return buffer.toByteArray();
    }
}
