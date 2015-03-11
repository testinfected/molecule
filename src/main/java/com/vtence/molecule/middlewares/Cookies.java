package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

public class Cookies extends AbstractMiddleware {
    public void handle(Request request, Response response) throws Exception {
        forward(request, response);
    }
}
