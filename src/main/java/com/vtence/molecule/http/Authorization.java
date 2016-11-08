package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import static com.vtence.molecule.http.HeaderNames.AUTHORIZATION;

public class Authorization {

    private final String scheme;
    private final String params;

    public static Authorization of(Request request) {
        String header = request.header(AUTHORIZATION);
        return header != null ? parse(header) : null;
    }

    public static Authorization parse(String header) {
        String[] tokens = header.split(" ");
        return new Authorization(tokens[0], tokens.length > 1 ? tokens[1]: "");
    }

    public Authorization(String scheme, String params) {
        this.scheme = scheme;
        this.params = params;
    }

    public String scheme() {
        return scheme;
    }

    public boolean hasScheme(String scheme) {
        return this.scheme.equals(scheme);
    }

    public String params() {
        return params;
    }
}
