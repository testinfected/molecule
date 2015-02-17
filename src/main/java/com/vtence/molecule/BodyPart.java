package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;

public class BodyPart {
    private final byte[] content;

    private String name;
    private String contentType;

    public BodyPart(byte[] content) {
        this.content = content;
    }

    public BodyPart name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public byte[] content() {
        return content;
    }

    public String text() {
        return new String(content, Charsets.UTF_8);
    }

    public BodyPart contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }
}
