package com.vtence.molecule;

public class Cookie {
    private final String name;

    private String value;
    private boolean httpOnly;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public boolean httpOnly() {
        return httpOnly;
    }

    public void httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
