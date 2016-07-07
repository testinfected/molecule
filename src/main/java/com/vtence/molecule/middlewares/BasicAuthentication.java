package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

public class BasicAuthentication extends AbstractMiddleware {
    public BasicAuthentication(String realm) {
    }

    public void handle(Request request, Response response) throws Exception {
        response.done();
    }
}
