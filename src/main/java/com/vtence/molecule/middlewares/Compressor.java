package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.BufferedResponse;

import java.io.IOException;
import java.util.List;
import java.util.zip.Deflater;

public class Compressor extends AbstractMiddleware {

    private static final String DEFLATE = "deflate";
    private static final int CHUNKS_SIZE = 512;

    public void handle(Request request, final Response response) throws Exception {
        BufferedResponse buffer = new BufferedResponse(response);
        forward(request, buffer);
        if (acceptableEncodingsFor(request).contains(DEFLATE)) {
            response.removeHeader(HttpHeaders.CONTENT_LENGTH);
            response.header(HttpHeaders.CONTENT_ENCODING, DEFLATE);
            deflate(response, buffer);
        } else {
            identity(response, buffer);
        }
    }

    private List<String> acceptableEncodingsFor(Request client) {
        return client.headers(HttpHeaders.ACCEPT_ENCODING);
    }

    private void deflate(Response response, BufferedResponse buffer) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(buffer.content());
        deflater.finish();
        byte[] buf = new byte[CHUNKS_SIZE];
        while (!deflater.needsInput()) {
            int written = deflater.deflate(buf);
            response.outputStream(written).write(buf);
        }
    }

    private void identity(Response response, BufferedResponse buffer) throws IOException {
        response.outputStream(buffer.size()).write(buffer.content());
    }
}
