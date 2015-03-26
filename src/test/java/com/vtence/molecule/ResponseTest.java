package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ResponseTest {

    Response response = new Response();

    @Test
    public void maintainsAnOrderedListOfHeaderNames() throws IOException {
        response.header("Accept-Encoding", "gzip");
        response.header("Accept-Language", "en");
        response.addHeader("Accept", "text/html");
        response.addHeader("Accept", "application/json");

        assertThat("header names", response.headerNames(), contains("Accept-Encoding", "Accept-Language", "Accept"));
    }

    @Test
    public void retrievesHeadersByName() throws IOException {
        response.addHeader("Accept", "text/html; q=0.9, application/json");
        assertThat("header", response.header("Accept"), equalTo("text/html; q=0.9, application/json"));
    }

    @Test
    public void convertsHeaderValuesToLongs() throws IOException {
        assertThat("Content-Length", response.headerAsLong("Content-Length"), equalTo(-1l));
        response.addHeader("Content-Length", "1258");
        assertThat("Content-Length", response.headerAsLong("Content-Length"), equalTo(1258l));
        assertThat("Content-Length", response.contentLength(), equalTo(1258l));
    }

    @Test
    public void canRemoveHeaders() throws IOException {
        response.header("Accept", "text/html");
        response.header("Accept-Encoding", "gzip");

        assertThat("header?", response.hasHeader("Accept"), equalTo(true));
        response.removeHeader("Accept");
        assertThat("still there?", response.hasHeader("Accept"), equalTo(false));

        assertThat("header names", response.headerNames(), contains("Accept-Encoding"));
    }

    @Test
    public void usesISO8859AsDefaultCharset() {
        assertThat("default charset", response.charset(), equalTo(Charsets.ISO_8859_1));
    }

    @Test
    public void readsCharsetFromContentType() {
        response.contentType("text/html; charset=utf-8");
        assertThat("charset", response.charset(), equalTo(Charsets.UTF_8));
    }

    @Test
    public void setsContentTypeCharset() {
        response.contentType("text/html; charset=iso-8859-1");
        response.charset("utf-8");
        assertThat("charset", response.contentType(), equalTo("text/html; charset=utf-8"));
        assertThat("charset", response.charset(), equalTo(Charsets.UTF_8));
    }

    @Test
    public void silentlyIgnoresCharsetWhenContentTypeNotSet() {
        response.charset("utf-8");
        response.contentType("text/html");
        assertThat("charset", response.contentType(), equalTo("text/html"));
    }
}