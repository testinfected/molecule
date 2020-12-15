package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.AcceptEncoding;
import com.vtence.molecule.http.ContentType;
import com.vtence.molecule.http.MimeTypes;
import com.vtence.molecule.lib.ChunkedBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.vtence.molecule.http.HeaderNames.CONTENT_ENCODING;
import static com.vtence.molecule.http.HeaderNames.CONTENT_LENGTH;
import static com.vtence.molecule.http.HttpStatus.NOT_ACCEPTABLE;
import static com.vtence.molecule.http.MimeTypes.TEXT;
import static com.vtence.molecule.middlewares.Compressor.Codings.identity;

public class Compressor implements Middleware {

    private final Collection<String> compressibleTypes = new ArrayList<>();

    enum Codings {

        gzip {
            public void encode(Response response) {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, name());
                response.body(new GZipStream(response.body()));
            }
        },

        deflate {
            public void encode(Response response) {
                response.removeHeader(CONTENT_LENGTH);
                response.header(CONTENT_ENCODING, name());
                response.body(new DeflateStream(response.body()));
            }
        },

        identity {
            public void encode(Response response) {
            }
        };

        public abstract void encode(Response response);

        public static String[] all() {
            List<String> all = new ArrayList<>();
            for (Codings coding : values()) {
                all.add(coding.name());
            }
            return all.toArray(new String[0]);
        }

        private static class GZipStream extends ChunkedBody {
            private final Body body;

            public GZipStream(Body body) {
                this.body = body;
            }

            public void writeTo(OutputStream out, Charset charset) throws IOException {
                GZIPOutputStream zip = new GZIPOutputStream(out);
                try {
                    body.writeTo(zip, charset);
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

            public void writeTo(OutputStream out, Charset charset) throws IOException {
                Deflater zlib = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
                DeflaterOutputStream deflate = new DeflaterOutputStream(out, zlib);
                try {
                    body.writeTo(deflate, charset);
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

    public Compressor compressibleTypes(String... mimeTypes) {
        this.compressibleTypes.addAll(Arrays.asList(mimeTypes));
        return this;
    }

    public Application then(Application next) {
        return request -> next.handle(request)
                              .whenSuccessful(compressResponse(selectBestAvailableEncodingFor(request)));
    }

    private Consumer<Response> compressResponse(String bestEncoding) {
        return response -> {
            if (unqualified(response)) {
                return;
            }

            if (bestEncoding != null) {
                Codings coding = Codings.valueOf(bestEncoding);
                coding.encode(response);
            } else {
                notAcceptable(response);
            }
        };
    }

    private boolean unqualified(Response response) {
        return empty(response) || alreadyEncoded(response) || !compressible(response);
    }

    private boolean empty(Response response) {
        return response.empty();
    }

    private boolean alreadyEncoded(Response response) {
        String contentEncoding = response.header(CONTENT_ENCODING);
        return contentEncoding != null && !isIdentity(contentEncoding);
    }

    private boolean compressible(Response response) {
        return compressibleTypes.isEmpty() || compressible(ContentType.of(response));
    }

    private boolean compressible(ContentType contentType) {
        return contentType != null && compressible(contentType.mediaType());
    }

    private boolean compressible(String mediaType) {
        for (String compressible : compressibleTypes) {
            if (MimeTypes.matches(mediaType, compressible)) return true;
        }
        return false;
    }

    private boolean isIdentity(String contentEncoding) {
        return contentEncoding.matches(atWordBoundaries(identity.name()));
    }

    private String atWordBoundaries(String text) {
        return "\\b" + text + "\\b";
    }

    private String selectBestAvailableEncodingFor(Request request) {
        AcceptEncoding acceptEncoding = AcceptEncoding.of(request);
        return acceptEncoding.selectBestEncoding(Codings.all());
    }

    private void notAcceptable(Response response) {
        response.status(NOT_ACCEPTABLE);
        response.contentType(TEXT);
        response.body("An acceptable encoding could not be found");
    }
}