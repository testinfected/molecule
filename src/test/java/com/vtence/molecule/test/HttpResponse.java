package com.vtence.molecule.test;

public class HttpResponse {
    private final byte[] content;

    public HttpResponse(byte[] content) {
        this.content = content;
    }

    public String bodyText() {
        return new String(content);
    }
}
