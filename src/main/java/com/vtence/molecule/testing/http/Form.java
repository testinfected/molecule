package com.vtence.molecule.testing.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class Form implements HttpContent {

    public static UrlEncodedForm urlEncoded() {
        return new UrlEncodedForm();
    }

    public static MultipartForm multipart() {
        return new MultipartForm();
    }

    protected final List<Field> fields = new ArrayList<>();

    public abstract Form addField(String name, String value);

    public Form addField(Field field) {
        this.fields.add(field);
        return this;
    }

    public interface Field {
        void encode(OutputStream out, Charset charset) throws IOException;
    }
}
