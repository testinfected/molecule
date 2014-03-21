package com.vtence.molecule.middlewares;

import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.AcceptEncoding;
import com.vtence.molecule.util.BufferedResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.vtence.molecule.HttpHeaders.CONTENT_ENCODING;
import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;

public class Compressor extends AbstractMiddleware {

    private enum Codings {

        gzip {
            public void encode(Response response, byte[] content) throws IOException {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, gzip.name());
                GZIPOutputStream out = new GZIPOutputStream(response.outputStream());
                out.write(content);
                out.finish();
            }
        },

        deflate {
            public void encode(Response response, byte[] content) throws IOException {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, name());
                Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
                DeflaterOutputStream out = new DeflaterOutputStream(response.outputStream(), deflater);
                out.write(content);
                out.finish();
                deflater.end();
            }
        },

        identity {
            public void encode(Response response, byte[] content) throws IOException {
                response.outputStream(content.length).write(content);
            }
        };

        public abstract void encode(Response to, byte[] content) throws IOException;

        public static String[] available() {
            List<String> available = new ArrayList<String>();
            for (Codings coding : values()) {
                available.add(coding.name());
            }
            return available.toArray(new String[available.size()]);
        }
    }

    public void handle(Request request, final Response response) throws Exception {
        BufferedResponse buffer = new BufferedResponse(response);
        forward(request, buffer);

        if (buffer.empty() || alreadyEncoded(response)) {
            buffer.flush();
            return;
        }

        String encoding = selectBestAvailableEncodingFor(request);
        if (encoding != null) {
            Codings coding = Codings.valueOf(encoding);
            coding.encode(response, buffer.content());
        } else {
            notAcceptable(response);
        }
    }

    private String selectBestAvailableEncodingFor(Request request) {
        AcceptEncoding acceptEncoding = AcceptEncoding.parse(request);
        return acceptEncoding.selectBestEncoding(Codings.available());
    }

    private boolean alreadyEncoded(Response response) {
        String contentEncoding = response.header(CONTENT_ENCODING);
        return contentEncoding != null && !isIdentity(contentEncoding);
    }

    private boolean isIdentity(String contentEncoding) {
        return contentEncoding.matches(atWordBoundaries(Codings.identity.name()));
    }

    private String atWordBoundaries(String text) {
        return "\\b" + text + "\\b";
    }

    private void notAcceptable(Response response) throws IOException {
        response.status(HttpStatus.NOT_ACCEPTABLE);
        response.contentType("text/plain");
        response.body("An acceptable encoding could not be found");
    }
}
