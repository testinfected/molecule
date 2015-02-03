package com.vtence.molecule.http;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MimeTypesTest {

    Map<String, String> knownTypes = new HashMap<String, String>();

    @Before public void
    defaultKnownTypes() {
        knownTypes.put("main.html", "text/html");
        knownTypes.put("plain.txt", "text/plain");
        knownTypes.put("application.css", "text/css");
        knownTypes.put("application.js", "application/javascript");
        knownTypes.put("logo.png", "image/png");
        knownTypes.put("fav.ico", "image/x-icon");
        knownTypes.put("image.gif", "image/gif");
        knownTypes.put("image.jpg", "image/jpeg");
        knownTypes.put("image.jpeg", "image/jpeg");
    }

    @Test public void
    guessesTypesFromFileExtension() {
        for (String filename : knownTypes.keySet()) {
            assertThat("media type of " + filename, MimeTypes.defaults().guessFrom(filename),
                    equalTo(knownTypes.get(filename)));
        }
    }

    @Test public void
    letsRedefineKnownTypes() {
        MimeTypes types = MimeTypes.defaults();
        types.register("css", "text/new-css");
        assertThat("media type", types.guessFrom("style.css"), equalTo("text/new-css"));
    }

    @Test public void
    canLearnNewTypes() {
        MimeTypes types = MimeTypes.defaults();
        types.register("bar", "application/bar");
        assertThat("media type", types.guessFrom("file.bar"), equalTo("application/bar"));
    }

    @Test public void
    assumesPlainTextWhenNotKnown() {
        MimeTypes types = new MimeTypes();
        assertThat("default media type", types.guessFrom("unknown"), equalTo("application/octet-stream"));
    }

    @Test public void
    matchesTypesThatAreIdentical() {
        assertThat("text/html is text/html", MimeTypes.matches("text/html", "text/html"), is(true));
        assertThat("text/html is text/plain", MimeTypes.matches("text/html", "text/plain"), is(false));
    }

    @Test public void
    matchesSubtypesAgainstWildCard() {
        assertThat("text/plain is text/*", MimeTypes.matches("text/plain", "text/*"), is(true));
        assertThat("text/html is text/*", MimeTypes.matches("text/html", "text/*"), is(true));
        assertThat("text/html is text", MimeTypes.matches("text/html", "text"), is(true));
        assertThat("application/json is text", MimeTypes.matches("application/json", "text"), is(false));
    }

    @Test public void
    matchesTypesAgainstWildcards() {
        assertThat("text/plain is */plain", MimeTypes.matches("text/plain", "*/plain"), is(true));
        assertThat("text/html is */html", MimeTypes.matches("text/html", "*/html"), is(true));
        assertThat("application/json is */html", MimeTypes.matches("application/json", "*/html"), is(false));
    }

    @Test public void
    matchesAgainstFullWildcards() {
        assertThat("text/plain is */*", MimeTypes.matches("text/plain", "*/*"), is(true));
        assertThat("text/html is */*", MimeTypes.matches("text/html", "*/*"), is(true));
        assertThat("text/plain is *", MimeTypes.matches("text/plain", "*"), is(true));
        assertThat("text/html is *", MimeTypes.matches("text/html", "*"), is(true));
    }
}