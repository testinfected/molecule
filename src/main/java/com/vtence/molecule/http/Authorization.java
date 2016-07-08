package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import static com.vtence.molecule.http.HeaderNames.AUTHORIZATION;

public class Authorization {

    private final String[] tokens;

    public static Authorization of(Request request) {
        return request.hasHeader(AUTHORIZATION) ? new Authorization(request.header(AUTHORIZATION)) : null;
    }

    public Authorization(String header) {
        this.tokens = header.split(" ");
    }

    public String scheme() {
        return tokens[0];
    }

    public boolean isForScheme(String scheme) {
        return scheme().equals(scheme);
    }

    public String params() {
        return tokens.length > 1 ? tokens[1]: "";
    }
}
