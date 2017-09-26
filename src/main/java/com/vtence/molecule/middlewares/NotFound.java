package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import static com.vtence.molecule.http.HttpStatus.NOT_FOUND;
import static com.vtence.molecule.http.MimeTypes.TEXT;

public class NotFound implements Application {

    public Response handle(Request request) throws Exception {
        return Response.of(NOT_FOUND)
                .contentType(TEXT)
                .done("Not found: " + request.path());
    }
}