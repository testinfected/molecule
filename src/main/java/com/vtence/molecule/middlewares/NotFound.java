package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.HttpStatus;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.MimeTypes;

public class NotFound implements Application {

    public static void notFound(Request request, Response response) throws Exception {
        new NotFound().handle(request, response);
    }

    public void handle(Request request, Response response) throws Exception {
        response.status(HttpStatus.NOT_FOUND);
        response.contentType(MimeTypes.TEXT);
        response.body("Not found: " + request.pathInfo());
    }
}
