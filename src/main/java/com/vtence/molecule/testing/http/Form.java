package com.vtence.molecule.testing.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class Form implements HttpRequest.BodyPublisher {

    public static UrlEncodedForm urlEncoded() {
        return new UrlEncodedForm();
    }

    public static MultipartForm multipart() {
        return new MultipartForm();
    }

    protected final List<Part> parts = new ArrayList<>();

    public abstract String contentType();

    protected void addPart(Part part) {
        this.parts.add(part);
    }

    public interface Part {
        void encode(OutputStream out, Charset charset) throws IOException;
    }
}
