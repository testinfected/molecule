package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Body;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.HexEncoder;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletionException;

import static com.vtence.molecule.http.HeaderNames.CACHE_CONTROL;
import static com.vtence.molecule.http.HeaderNames.ETAG;
import static com.vtence.molecule.lib.BinaryBody.bytes;

public class ETag implements Middleware {

    private static final String REVALIDATE = "max-age=0; private; no-cache";
    private final HexEncoder encoder = new HexEncoder();

    public Application then(Application next) {
        return request -> next.handle(request).whenSuccessful(this::computeETag);
    }

    private void computeETag(Response response) {
        if (!isCacheable(response)) return;
        if (!hasCachingDirective(response)) response.header(CACHE_CONTROL, REVALIDATE);

        try {
            byte[] output = render(response);
            response.header(ETAG, etagOf(output));
            response.body(bytes(output));
        } catch (Exception wontHappen) {
            throw new CompletionException(wontHappen);
        }
    }

    private String etagOf(byte[] output) throws NoSuchAlgorithmException {
        return "\"" + encoder.toHex(computeHash(output)) + "\"";
    }

    private byte[] render(Response response) throws IOException {
        try(var out = new ByteArrayOutputStream()) {
            try(Body body = response.body()) {
                body.writeTo(out, response.charset());
            }
            return out.toByteArray();
        }
    }

    private boolean isCacheable(Response response) {
        return hasContent(response) && hasCacheableStatus(response) && !skipCaching(response);
    }

    private boolean hasContent(Response response) {
        return !response.empty();
    }

    private boolean hasCacheableStatus(Response response) {
        return response.statusCode() == HttpStatus.OK.code || response.statusCode() == HttpStatus.CREATED.code;
    }

    private boolean skipCaching(Response response) {
        return hasETag(response)
                || hasLastModified(response)
                || preventsCaching(response);
    }

    private boolean hasETag(Response response) {
        return response.hasHeader(ETAG);
    }

    private boolean hasLastModified(Response response) {
        return response.hasHeader(HeaderNames.LAST_MODIFIED);
    }

    private boolean preventsCaching(Response response) {
        return hasCachingDirective(response) && cachingDirective(response).contains("no-cache");
    }

    private boolean hasCachingDirective(Response response) {
        return response.hasHeader(CACHE_CONTROL);
    }

    private String cachingDirective(Response response) {
        return response.header(CACHE_CONTROL);
    }

    private byte[] computeHash(byte[] output) throws NoSuchAlgorithmException {
        var md5 = MessageDigest.getInstance("MD5");
        return md5.digest(output);
    }
}