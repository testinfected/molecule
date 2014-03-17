package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.ResponseWrapper;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.DeflaterOutputStream;

public class Compressor extends AbstractMiddleware {

    public void handle(Request request, final Response response) throws Exception {
        DeflatingResponse deflater = new DeflatingResponse(response);
        successor.handle(request, deflater);
        deflater.finish();
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

        public void body(String body) throws IOException {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream(), charset()));
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
