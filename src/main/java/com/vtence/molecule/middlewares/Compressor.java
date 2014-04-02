package com.vtence.molecule.middlewares;

import com.vtence.molecule.Body;
import com.vtence.molecule.ChunkedBody;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.AcceptEncoding;
import com.vtence.molecule.util.BufferedResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.vtence.molecule.HttpHeaders.CONTENT_ENCODING;
import static com.vtence.molecule.HttpHeaders.CONTENT_LENGTH;
import static com.vtence.molecule.middlewares.Compressor.Codings.identity;

public class Compressor extends AbstractMiddleware {

    static enum Codings {

        gzip {
            public void encode(Response response, Body body) throws IOException {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, name());
                response.body(new GZipStream(body));
            }
        },

        deflate {
            public void encode(Response response, Body body) throws IOException {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, name());
                response.body(new DeflateStream(body));
            }
        },

        identity {
            public void encode(Response response, Body body) throws IOException {
                response.body(body);
            }
        };

        public abstract void encode(Response to, Body body) throws IOException;

        public static String[] available() {
            List<String> available = new ArrayList<String>();
            for (Codings coding : values()) {
                available.add(coding.name());
            }
            return available.toArray(new String[available.size()]);
        }

        private static class GZipStream extends ChunkedBody {
            private final Body body;

            public GZipStream(Body body) {
                this.body = body;
            }

            public void writeTo(OutputStream out) throws IOException {
                GZIPOutputStream zip = new GZIPOutputStream(out);
                try {
                    body.writeTo(zip);
                } finally {
                    zip.finish();
                }
            }

            public void close() throws IOException {
                body.close();
            }
        }

        private static class DeflateStream extends ChunkedBody {
            private final Body body;

            public DeflateStream(Body body) {
                this.body = body;
            }

            public void writeTo(OutputStream out) throws IOException {
                Deflater zlib = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
                DeflaterOutputStream deflate = new DeflaterOutputStream(out, zlib);
                try {
                    body.writeTo(deflate);
                } finally {
                    deflate.finish();
                    zlib.end();
                }
            }

            public void close() throws IOException {
                body.close();
            }
        }
    }

    public void handle(Request request, final Response response) throws Exception {
        BufferedResponse buffer = new BufferedResponse(response);
        forward(request, buffer);

        if (buffer.empty() || alreadyEncoded(response)) {
            response.body(buffer.body());
            return;
        }

        String encoding = selectBestAvailableEncodingFor(request);
        if (encoding != null) {
            Codings coding = Codings.valueOf(encoding);
            coding.encode(response, buffer.body());
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
        return contentEncoding.matches(atWordBoundaries(identity.name()));
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
