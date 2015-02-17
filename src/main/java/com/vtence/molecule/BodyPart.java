package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;

public class BodyPart {
    private final String name;
    private final byte[] content;

    public BodyPart(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String name() {
        return name;
    }

    public String value() {
        return new String(content, Charsets.UTF_8);
    }
}
