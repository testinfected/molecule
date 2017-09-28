package com.vtence.molecule.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class Uri {

    private final String scheme;
    private final String userInfo;
    private final String host;
    private final int port;
    private final String path;
    private final String query;
    private final String fragment;

    public Uri(String scheme, String userInfo, String host, int port, String path, String query, String fragment) {
        this.scheme = scheme;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }

    public static Uri of(String value) {
        URI uri = URI.create(value);
        return new Uri(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    public static Uri from(URI uri) {
        return new Uri(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    public String scheme() {
        return scheme;
    }

    public Uri scheme(String scheme) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String userInfo() {
        return userInfo;
    }

    public Uri userInfo(String userInfo) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String host() {
        return host;
    }

    public Uri host(String host) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public int port() {
        return port;
    }

    public Uri port(int port) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String path() {
        return path;
    }

    public Uri path(String path) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String query() {
        return query;
    }

    public Uri query(String query) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String fragment() {
        return fragment;
    }

    public Uri fragment(String fragment) {
        return new Uri(scheme, userInfo, host, port, path, query, fragment);
    }

    public String uri() {
        if (path == null) return "";
        return (path.isEmpty() ? "/" : path) + queryComponent() + fragmentComponent();
    }

    public String queryComponent() {
        return query == null ? "" : "?" + query;
    }

    public String fragmentComponent() {
        return fragment == null ? "" : "#" + fragment;
    }

    public Uri normalize() {
        return from(toURI().normalize());
    }

    public URI toURI() {
        try {
            return new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public URL toURL() {
        try {
            return toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String toString() {
        return toURI().toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uri uri = (Uri) o;
        return port == uri.port &&
               Objects.equals(scheme, uri.scheme) &&
               Objects.equals(userInfo, uri.userInfo) &&
               Objects.equals(host, uri.host) &&
               Objects.equals(path, uri.path) &&
               Objects.equals(query, uri.query) &&
               Objects.equals(fragment, uri.fragment);
    }

    public int hashCode() {
        return Objects.hash(scheme, userInfo, host, port, path, query, fragment);
    }
}
