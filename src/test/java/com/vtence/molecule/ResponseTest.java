package com.vtence.molecule;

import com.vtence.molecule.helpers.Charsets;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LANGUAGE;
import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static java.util.Locale.US;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;

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
    public void maintainsAListOfCookies() {
        response.cookie("mr christie", "peanuts");
        response.cookie("petit ecolier", "chocolat noir");
        response.cookie("delicious", "chocolat au lait");

        assertThat("cookies", response.cookieNames(), containsInAnyOrder("mr christie", "petit ecolier", "delicious"));
    }

    @Test
    public void canRemoveCookies() {
        response.cookie("mr christie", "peanuts");
        response.cookie("petit ecolier", "chocolat noir");
        assertThat("cookie?", response.hasCookie("mr christie"), equalTo(true));
        response.removeCookie("mr christie");
        assertThat("still there?", response.hasCookie("mr christie"), equalTo(false));

        assertThat("cookies", response.cookieNames(), contains("petit ecolier"));
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

    @Test
    public void hasNoLocaleByDefault() {
        assertThat("default locale", response.locale(), nullValue());
    }

    @Test
    public void maintainsAnOrderedListOfLocales() {
        response.addLocale(CANADA_FRENCH);
        response.addLocale(US);
        response.addLocale(FRENCH);
        assertThat("locales", response.locales(), contains(CANADA_FRENCH, US, FRENCH));
        assertThat("content-language", response.header(CONTENT_LANGUAGE), equalTo("fr-ca, en-us, fr"));
    }

    @Test
    public void setsContentLanguageToSpecifiedLocale() {
        response.locale(FRENCH);
        assertThat("locale", response.locale(), equalTo(FRENCH));
        assertThat("content-language", response.header(CONTENT_LANGUAGE), equalTo("fr"));
    }

    @Test
    public void removesLocalesOnDemand() {
        response.addLocale(CANADA_FRENCH);
        response.addLocale(ENGLISH);
        response.removeLocale(CANADA_FRENCH);
        assertThat("new preferred locale", response.locale(), equalTo(ENGLISH));
        assertThat("content-language", response.header(CONTENT_LANGUAGE), equalTo("en"));
    }
}