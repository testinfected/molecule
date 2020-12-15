package com.vtence.molecule.http;

public class Cookie {
    private final String name;
    private final String value;

    private int version = 1;
    private int maxAge = -1;
    private String domain;
    private String path = "/";
    private boolean secure;
    private boolean httpOnly;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public int version() {
        return version;
    }

    public Cookie version(int version) {
        this.version = version;
        return this;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public Cookie maxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public int maxAge() {
        return maxAge;
    }

    public String domain() {
        return domain;
    }

    public Cookie domain(String domain) {
        this.domain = domain;
        return this;
    }

    public Cookie path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }

    public Cookie secure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean secure() {
        return secure;
    }

    public boolean httpOnly() {
        return httpOnly;
    }

    public Cookie httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public String toString() {
        return name + "=" + value +
                "; version=" + version +
                (path != null ? "; path=" + path : "") +
                (domain != null ? "; domain=" + domain : "") +
                (maxAge >= 0 ? "; max-age=" + maxAge : "") +
                (secure ? "; secure" : "") +
                (httpOnly ? "; httponly" : "");
    }
}
