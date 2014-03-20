package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.BufferedResponse;

import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.vtence.molecule.HttpHeaders.CONTENT_ENCODING;

public class Compressor extends AbstractMiddleware {

    private enum Coding {

        deflate {
            public void encode(Response to, byte[] content) throws IOException {
                Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
                DeflaterOutputStream out = new DeflaterOutputStream(to.outputStream(), deflater);
                out.write(content);
                out.finish();
                deflater.end();
            }
        },

        gzip {
            public void encode(Response to, byte[] content) throws IOException {
                to.removeHeader(HttpHeaders.CONTENT_LENGTH);
                to.header(CONTENT_ENCODING, gzip.name());
                GZIPOutputStream out = new GZIPOutputStream(to.outputStream());
                out.write(content);
                out.finish();
            }
        };

        public boolean matches(String encoding) {
            return name().equalsIgnoreCase(encoding);
        }

        public abstract void encode(Response out, byte[] content) throws IOException;
    }

    public void handle(Request request, final Response response) throws Exception {
        BufferedResponse buffer = new BufferedResponse(response);
        forward(request, buffer);

        if (buffer.empty() || alreadyEncoded(response) && !identityEncoded(response)) {
            writeUncompressed(response, buffer);
            return;
        }

        for (String encoding: acceptableEncodingsFor(request)) {
            for (Coding coding : Coding.values()) {
                if (coding.matches(encoding)) {
                    response.removeHeader(HttpHeaders.CONTENT_LENGTH);
                    response.header(HttpHeaders.CONTENT_ENCODING, coding.name());
                    coding.encode(response, buffer.content());
                    return;
                }
            }
        }

        writeUncompressed(response, buffer);
    }

    private boolean alreadyEncoded(Response response) {
        return response.header(HttpHeaders.CONTENT_ENCODING) != null;
    }

    private boolean identityEncoded(Response response) {
        return response.header(HttpHeaders.CONTENT_ENCODING).matches("\\bidentity\\b");
    }

    private void writeUncompressed(Response response, BufferedResponse buffer) throws IOException {
        response.outputStream(buffer.size()).write(buffer.content());
    }

    private List<String> acceptableEncodingsFor(Request client) {
        // This works fine because Simple does the job of parsing the header for use
        // and removes non acceptable codings
        return client.headers(HttpHeaders.ACCEPT_ENCODING);
    }
}
