package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

public class URLMap extends AbstractMiddleware {
    public URLMap() {
        this(new NotFound());
    }

    public URLMap(Application fallback) {
        defaultTo(fallback);
    }

    private URLMap defaultTo(Application fallback) {
        connectTo(fallback);
        return this;
    }

    public URLMap mount(String path, Application application) {
        return this;
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        forward(request, response);
    }
}