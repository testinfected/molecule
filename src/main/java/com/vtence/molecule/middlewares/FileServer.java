package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpHeaders;
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

import static com.vtence.molecule.middlewares.NotFound.notFound;

public class FileServer implements Application {

    private final File root;
    private final MimeTypes mediaTypes = MimeTypes.defaults();

    public FileServer(File root) {
        this.root = root;
    }

    public void registerMediaType(String extension, String mediaType) {
        mediaTypes.map(extension, mediaType);
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

        response.contentType(mediaTypes.guessFrom(file.getName()));
        response.headerDate(HttpHeaders.LAST_MODIFIED, file.lastModified());
        response.contentLength(file.length());

        InputStream in = new FileInputStream(file);
        try {
            Streams.copy(in, response.outputStream());
        } finally {
            Streams.close(in);
        }

        response.status(HttpStatus.OK);
    }

    private boolean notModifiedSince(Request request, File file) {
        return HttpDate.format(file.lastModified()).equals(request.header("If-Modified-Since"));
    }

}
