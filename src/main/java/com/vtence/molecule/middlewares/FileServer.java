package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Joiner;
import com.vtence.molecule.http.HttpDate;
import com.vtence.molecule.http.HttpMethod;
import com.vtence.molecule.http.MimeTypes;
import com.vtence.molecule.lib.FileBody;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vtence.molecule.http.HeaderNames.ALLOW;
import static com.vtence.molecule.http.HeaderNames.IF_MODIFIED_SINCE;
import static com.vtence.molecule.http.HeaderNames.LAST_MODIFIED;
import static com.vtence.molecule.http.HttpMethod.GET;
import static com.vtence.molecule.http.HttpMethod.HEAD;
import static com.vtence.molecule.http.HttpStatus.METHOD_NOT_ALLOWED;
import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.HttpStatus.NOT_MODIFIED;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class FileServer implements Application {

    private final File root;
    private final MimeTypes mediaTypes = MimeTypes.defaults();
    private final Map<String, String> headers = new HashMap<>();

    private static final List<HttpMethod> ALLOWED_METHODS = Arrays.asList(GET, HEAD);
    private static final String ALLOW_HEADER = Joiner.on(", ").join(ALLOWED_METHODS);

    public FileServer(File root) {
        this.root = root;
    }

    public void registerMediaType(String extension, String mediaType) {
        mediaTypes.register(extension, mediaType);
    }

    public FileServer header(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public Response handle(Request request) throws Exception {
        if (!methodAllowed(request)) {
            return Response.of(METHOD_NOT_ALLOWED)
                           .header(ALLOW, ALLOW_HEADER)
                           .done();
        }

        var file = new File(root, request.path());
        if (!canServe(file)) {
            return Response.of(NOT_FOUND)
                           .contentType(TEXT)
                           .body("File not found: " + request.path())
                           .done();
        }

        if (notModifiedSince(lastTimeSeen(request), file)) {
            return Response.of(NOT_MODIFIED)
                           .done();
        }

        var response = Response.ok();
        addFileHeaders(response, file);
        addCustomHeaders(response);

        if (head(request)) {
            return response.done();
        }

        return response.done(new FileBody(file));
    }

    private boolean canServe(File file) {
        return file.exists() && file.canRead() && !file.isDirectory();
    }

    private boolean methodAllowed(Request request) {
        return ALLOWED_METHODS.contains(request.method());
    }

    private boolean notModifiedSince(String date, File file) {
        return HttpDate.format(file.lastModified()).equals(date);
    }

    private String lastTimeSeen(Request request) {
        return request.header(IF_MODIFIED_SINCE);
    }

    private void addFileHeaders(Response response, File file) {
        response.contentType(mediaTypes.guessFrom(file.getName()));
        response.header(LAST_MODIFIED, Instant.ofEpochMilli(file.lastModified()));
        response.contentLength(file.length());
    }

    private void addCustomHeaders(Response response) {
        for (String header : headers.keySet()) {
            response.header(header, headers.get(header));
        }
    }

    private boolean head(Request request) {
        return request.method() == HEAD;
    }
}