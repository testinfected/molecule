package com.vtence.molecule.http;

public enum HttpMethod {
    OPTIONS, GET, HEAD, POST, PUT, DELETE, PATCH;

    public static boolean valid(String method) {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) return true;
        }
        return false;
    }
}