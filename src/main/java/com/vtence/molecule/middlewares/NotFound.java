package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class NotFound implements Application {

    public void handle(Request request, Response response) throws Exception {
        response.status(NOT_FOUND);
        response.contentType(TEXT);
        response.body("Not found: " + request.path());
    }
}
