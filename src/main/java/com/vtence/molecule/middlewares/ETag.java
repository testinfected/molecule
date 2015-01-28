package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.HeaderNames;
import com.vtence.molecule.http.HttpStatus;
import com.vtence.molecule.lib.AbstractMiddleware;
import com.vtence.molecule.lib.BinaryBody;
import com.vtence.molecule.lib.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.vtence.molecule.http.HeaderNames.CACHE_CONTROL;
import static com.vtence.molecule.http.HeaderNames.ETAG;

public class ETag extends AbstractMiddleware {

    private static final String REVALIDATE = "max-age=0; private; no-cache";

    public void handle(Request request, Response response) throws Exception {
        forward(request, response);

        byte[] output = render(response);
        if (isCacheable(response, output)) {
            response.header(ETAG, "\"" + Hex.from(computeHash(output)) + "\"");
        }
        if (!hasCachingDirective(response)) {
            response.header(CACHE_CONTROL, REVALIDATE);
        }
        response.body(new BinaryBody(output));
    }

    private byte[] render(Response response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.body().writeTo(out, response.charset());
        out.close();
        return out.toByteArray();
    }

    private boolean isCacheable(Response response, byte[] output) {
        return hasContent(output) && hasCacheableStatus(response) && !skipCaching(response);
    }

    private boolean hasContent(byte[] output) {
        return output.length > 0;
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
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(output);
    }
}