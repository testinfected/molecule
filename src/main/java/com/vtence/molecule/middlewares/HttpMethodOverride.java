package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.http.HttpMethod;

import java.io.IOException;

import static com.vtence.molecule.http.HttpMethod.POST;

public class HttpMethodOverride implements Middleware {

    public static final String METHOD_OVERRIDE_PARAMETER = "_method";

    public Application then(Application next) {
        return request -> {
            if (overrideDetected(request) && validOverride(request)) {
                request.method(methodOverride(request).toUpperCase());
            }
            return next.handle(request);
        };
    }

    private boolean validOverride(Request request) {
        return HttpMethod.valid(methodOverride(request));
    }

    private boolean overrideDetected(Request request) throws IOException {
        return methodOverride(request) != null && request.method() == POST;
    }

    private String methodOverride(Request request) {
        return request.parameter(METHOD_OVERRIDE_PARAMETER);
    }
}