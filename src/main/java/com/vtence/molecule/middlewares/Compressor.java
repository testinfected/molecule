package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.ResponseWrapper;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public class Compressor extends AbstractMiddleware {

    private static final String DEFLATE = "deflate";

    public void handle(Request request, final Response response) throws Exception {
        if (acceptableEncodingsFor(request).contains(DEFLATE)) {
            response.header(HttpHeaders.CONTENT_ENCODING, DEFLATE);
            DeflatingResponse deflater = new DeflatingResponse(response);
            forward(request, deflater);
            deflater.finish();
        } else {
            forward(request, response);
        }
    }

    private List<String> acceptableEncodingsFor(Request request) {
        return request.headers(HttpHeaders.ACCEPT_ENCODING);
    }

    private static class DeflatingResponse extends ResponseWrapper {
        private final Response response;
        private DeflaterOutputStream deflater;

        public DeflatingResponse(Response response) {
            super(response);
            this.response = response;
        }

        public OutputStream outputStream() throws IOException {
            if (deflater == null) {
                deflater = new DeflaterOutputStream(response.outputStream());
            }
            return deflater;
        }

        public OutputStream outputStream(int bufferSize) throws IOException {
            return new BufferedOutputStream(outputStream(), bufferSize);
        }

        public Writer writer() throws IOException {
            return new OutputStreamWriter(outputStream(), charset());
        }

        public void body(String body) throws IOException {
            Writer writer = new BufferedWriter(writer());
            writer.write(body);
            writer.flush();
        }

        public void finish() throws IOException {
            if (deflater != null) {
                deflater.finish();
            }
        }
    }
}
