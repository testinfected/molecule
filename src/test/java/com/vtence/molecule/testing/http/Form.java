package com.vtence.molecule.testing.http;

public abstract class Form implements HttpContent {

    public static UrlEncodedForm urlEncoded() {
        return new UrlEncodedForm();
    }

    public static MultipartForm multipart() {
        return new MultipartForm();
    }
}
