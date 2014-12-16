package com.vtence.molecule.http;

import org.junit.Test;

import static java.util.Locale.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AcceptLanguageTest {

    @Test public void
    prefersNoLocaleWhenEmpty() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("");
        assertThat("no locale", acceptLanguage.locales(), empty());
    }

    @Test public void
    prefersSpecifiedNaturalLanguage() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr");
        assertThat("locales", acceptLanguage.locales(), contains(FRENCH));
    }

    @Test public void
    prefersSpecifiedNaturalLanguageAndCountry() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr-ca");
        assertThat("locales", acceptLanguage.locales(), contains(CANADA_FRENCH));
    }

    @Test public void
    canSpecifySeveralLocales() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr-ca, en");
        assertThat("locales", acceptLanguage.locales(), contains(CANADA_FRENCH, ENGLISH));
    }
}