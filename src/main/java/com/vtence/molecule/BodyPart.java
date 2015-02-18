package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import com.vtence.molecule.helpers.Streams;

import java.io.IOException;
import java.io.InputStream;

public class BodyPart {
    private final InputStream input;

    private String name;
    private String filename;
    private String contentType;

    public BodyPart(InputStream input) {
        this.input = input;
    }

    public BodyPart name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public BodyPart filename(String filename) {
        this.filename = filename;
        return this;
    }

    public String filename() {
        return filename;
    }

    public byte[] content() throws IOException {
        return Streams.toBytes(input);
    }

    public String text() throws IOException {
        return new String(content(), Charsets.UTF_8);
    }

    public BodyPart contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }
}
