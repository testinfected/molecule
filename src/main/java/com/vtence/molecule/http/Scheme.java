package com.vtence.molecule.http;

public final class Scheme {

    public static final Scheme HTTP = new Scheme("http", 80);
    public static final Scheme HTTPS = new Scheme("https", 443);

    private static final Scheme[] KNOWN = new Scheme[] { HTTP, HTTPS };

    private final String name;
    private final int defaultPort;

    private Scheme(String name, int defaultPort) {
        this.name = name;
        this.defaultPort = defaultPort;
    }

    public String name() {
        return name;
    }

    public int defaultPort() {
        return defaultPort;
    }

    public String toString() {
        return name;
    }

    public static Scheme of(String name) {
        for (Scheme scheme : KNOWN) {
            if (scheme.name.equalsIgnoreCase(name)) return scheme;
        }
        return new Scheme(name, -1);
    }

    public static Scheme from(Uri uri) {
        return of(uri.scheme());
    }
}
