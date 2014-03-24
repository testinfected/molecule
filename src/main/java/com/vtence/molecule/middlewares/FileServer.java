package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.HttpDate;
import com.vtence.molecule.util.MimeTypes;
import com.vtence.molecule.util.Streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.vtence.molecule.HttpHeaders.IF_MODIFIED_SINCE;
import static com.vtence.molecule.HttpHeaders.LAST_MODIFIED;
import static com.vtence.molecule.middlewares.NotFound.notFound;

public class FileServer implements Application {

    private final File root;
    private final MimeTypes mediaTypes = MimeTypes.defaults();
    private final Map<String, String> headers = new HashMap<String, String>();

    public FileServer(File root) {
        this.root = root;
    }

    public void registerMediaType(String extension, String mediaType) {
        mediaTypes.map(extension, mediaType);
    }

    public FileServer addHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        try {
            renderFile(request, response);
        } catch (FileNotFoundException e) {
            notFound(request, response);
        }
    }

    private void renderFile(Request request, Response response) throws IOException {
        File file = new File(root, request.pathInfo());

        if (notModifiedSince(request, file)) {
            response.status(HttpStatus.NOT_MODIFIED);
            return;
        }

        addFileHeaders(response, file);
        addCustomHeaders(response);
        serve(response, file);

        response.status(HttpStatus.OK);
    }

    private void serve(Response response, File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            Streams.copy(in, response.outputStream());
        } finally {
            Streams.close(in);
        }
    }

    private void addFileHeaders(Response response, File file) {
        response.contentType(mediaTypes.guessFrom(file.getName()));
        response.headerDate(LAST_MODIFIED, file.lastModified());
        response.contentLength(file.length());
    }

    private void addCustomHeaders(Response response) {
        for (String header : headers.keySet()) {
            response.header(header, headers.get(header));
        }
    }

    private boolean notModifiedSince(Request request, File file) {
        return HttpDate.format(file.lastModified()).equals(request.header(IF_MODIFIED_SINCE));
    }
}
