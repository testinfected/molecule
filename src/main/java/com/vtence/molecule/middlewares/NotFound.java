package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.Charsets;
import com.vtence.molecule.util.MimeTypes;

public class NotFound implements Application {

    public void handle(Request request, Response response) throws Exception {
        response.status(HttpStatus.NOT_FOUND);
        String body = "Not found: " + request.pathInfo();
        byte[] bytes = body.getBytes(Charsets.ISO_8859_1);
        response.contentType(MimeTypes.TEXT);
        response.contentLength(bytes.length);
        response.outputStream(bytes.length).write(bytes);
    }
}
