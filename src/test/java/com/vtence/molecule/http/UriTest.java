package com.vtence.molecule.http;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class UriTest {

    @Test
    public void constructsFromJavaURI() {
        Uri uri = Uri.from(URI.create("https://user:password@example.com:8443/path?query#fragment"));

        assertThat("scheme", uri.scheme(), is("https"));
        assertThat("user info", uri.userInfo(), is("user:password"));
        assertThat("host", uri.host(), is("example.com"));
        assertThat("port", uri.port(), is(8443));
        assertThat("path", uri.path(), is("/path"));
        assertThat("query", uri.query(), is("query"));
        assertThat("fragment", uri.fragment(), is("fragment"));
    }

    @Test
    public void buildsFromIndividualComponents() {
        Uri uri = new Uri("https", "user:password", "example.com", 8443, "/path", "query", "fragment");

        assertThat("scheme", uri.scheme(), is("https"));
        assertThat("user info", uri.userInfo(), is("user:password"));
        assertThat("host", uri.host(), is("example.com"));
        assertThat("port", uri.port(), is(8443));
        assertThat("path", uri.path(), is("/path"));
        assertThat("query", uri.query(), is("query"));
        assertThat("fragment", uri.fragment(), is("fragment"));
    }

    @Test
    public void providesConvenientMethods() {
        Uri uri = Uri.of("/path?query#fragment");

        assertThat("uri", uri.uri(), is("/path?query#fragment"));
        assertThat("query component", uri.queryComponent(), is("?query"));
        assertThat("fragment component", uri.fragmentComponent(), is("#fragment"));
    }

    @Test
    public void handlesAbsenceOfPath() {
        Uri uri = Uri.of("http://localhost");

        assertThat("uri", uri.uri(), is("/"));
    }

    @Test
    public void handlesNullPath() {
        Uri uri = new Uri(null, null, null, -1, null, null, null);

        assertThat("uri", uri.uri(), is(""));
    }

    @Test
    public void handlesAbsenceOfQueryComponent() {
        Uri uri = new Uri(null, null, null, -1, "/path", null, null);

        assertThat("query component", uri.queryComponent(), is(""));
    }

    @Test
    public void ignoresEmptyQueryString() {
        Uri uri = new Uri(null, null, null, -1, "/path", "", null);

        assertThat("query component", uri.queryComponent(), is(""));
        assertThat("fragment component", uri.fragmentComponent(), is(""));
    }

    @Test
    public void convertsToJavaURI() {
        Uri uri = Uri.of("https://user@example.com/path?query#fragment");

        assertThat(uri.toURI(), equalTo(URI.create("https://user@example.com/path?query#fragment")));
    }

    @Test
    public void convertsToJavaURL() throws MalformedURLException {
        Uri uri = Uri.of("https://user@example.com/path?query#fragment");

        assertThat(uri.toURL(), equalTo(new URL("https://user@example.com/path?query#fragment")));
    }

    @Test
    public void normalizesItsPath() {
        Uri uri = Uri.of("/path/to/to/../file");

        assertThat("normalized path", uri.normalize().path(), is("/path/to/file"));
    }

    @Test
    public void hasTextRepresentation() {
        Uri uri = Uri.of("https://user:password@example.com:8443/path?query#fragment");

        assertThat("repr", uri.toString(), is("https://user:password@example.com:8443/path?query#fragment"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void raiseExceptionOnURIConversionIfInvalid() {

        new Uri("http", null, null, -1, null, null, null)
                .toURI();
    }

    @Test(expected = IllegalArgumentException.class)
    public void raiseExceptionOnURLConversionIfUnsupported() {

        new Uri("unsupported", null, null, -1, "/", null, null)
                .toURL();
    }

    @Test
    public void isImmutable() {
        Uri original = Uri.of("https://user@example.com/path?query#fragment");

        assertThat(original.scheme("http")
                           .userInfo(null)
                           .host("localhost")
                           .port(8080)
                           .path("")
                           .query(null)
                           .fragment(null), equalTo(Uri.of("http://localhost:8080")));

        assertThat(original, equalTo(Uri.of("https://user@example.com/path?query#fragment")));
    }

    @Test
    public void definesEquality() {
        Uri uri = Uri.of("https://user@example.com/path?query#fragment");
        assertThat("same uri", uri, equalTo(Uri.of("https://user@example.com/path?query#fragment")));

        assertThat("on scheme", uri, not(equalTo(uri.scheme("http"))));
        assertThat("on user info", uri, not(equalTo(uri.userInfo("other"))));
        assertThat("on host", uri, not(equalTo(uri.host("localhost"))));
        assertThat("on port", uri, not(equalTo(uri.port(8080))));
        assertThat("on path", uri, not(equalTo(uri.path("/"))));
        assertThat("on query", uri, not(equalTo(uri.query(null))));
        assertThat("on fragment", uri, not(equalTo(uri.fragment(null))));
    }

    @Test
    public void isConsistentWithHashCode() {
        String spec = "https://user@example.com/path?query#fragment";

        assertThat("hash code", Uri.of(spec).hashCode(), equalTo(Uri.of(spec).hashCode()));
    }
}