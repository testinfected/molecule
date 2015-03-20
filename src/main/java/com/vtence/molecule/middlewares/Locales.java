package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.util.Locale;

public class Locales extends AbstractMiddleware {

    public Locales(String... supported) {
    }

    public void handle(Request request, Response response) throws Exception {
        request.attribute(Locale.class, Locale.getDefault());
        forward(request, response);
    }
}
