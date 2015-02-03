package com.vtence.molecule.http;

import org.junit.Test;

import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ContentLanguageTest {

    @Test public void
    describesNoLocaleWhenEmpty() {
        ContentLanguage contentLanguage = ContentLanguage.parse("");
        assertThat("no locale", contentLanguage.locales(), empty());
    }

    @Test public void
    describesNaturalLanguageOfContent() {
        ContentLanguage contentLanguage = ContentLanguage.parse("fr");
        assertThat("locales", contentLanguage.locales(), contains(FRENCH));
    }

    @Test public void
    describesNaturalLanguageAndCountry() {
        ContentLanguage contentLanguage = ContentLanguage.parse("fr-ca");
        assertThat("locales", contentLanguage.locales(), contains(CANADA_FRENCH));
    }

    @Test public void
    describesSeveralLocales() {
        ContentLanguage contentLanguage = ContentLanguage.parse("fr-ca, en");
        assertThat("locales", contentLanguage.locales(), contains(CANADA_FRENCH, ENGLISH));
    }

    @Test public void
    hasAStringRepresentation() {
        ContentLanguage contentLanguage = ContentLanguage.parse("fr-ca, en");
        assertThat("header", contentLanguage.toString(), equalTo("fr-ca, en"));
    }

    @Test public void
    suppressCoverageNoise() {
        new LanguageTag();
    }
}