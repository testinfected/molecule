package com.vtence.molecule.http;

import org.junit.Test;

import static java.util.Locale.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AcceptLanguageTest {

    @Test public void
    prefersNoLocaleWhenEmpty() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("");
        assertThat("no locale", acceptLanguage.list(), empty());
    }

    @Test public void
    parsesNaturalLanguage() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr");
        assertThat("all locales", acceptLanguage.list(), contains(FRENCH));
    }

    @Test public void
    parsesNaturalLanguageAndCountry() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr-ca");
        assertThat("all locales", acceptLanguage.list(), contains(CANADA_FRENCH));
    }

    @Test public void
    listsAcceptableLocalesInPreferenceOrder() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0.8, fr-ca");
        assertThat("all locales", acceptLanguage.list(), contains(CANADA_FRENCH, ENGLISH));
    }

    @Test public void
    selectsNoLanguageWhenThereIsNoCandidate() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en");
        assertThat("best locale", acceptLanguage.selectBest(), nullValue());
    }

    @Test
    public void
    selectsAcceptableLanguageWithHighestQuality() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0.8, fr-ca");
        assertThat("best locale", acceptLanguage.selectBest("en", "fr-CA"), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsFirstAmongstSupportedLocalesOfSameQuality() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0.8, fr-ca, es-es");
        assertThat("best locale", acceptLanguage.selectBest("fr-CA", "es-ES"), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsNoLocaleWhenCandidatesAreNotAcceptable() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0, fr-ca");
        assertThat("best locale", acceptLanguage.selectBest("en"), nullValue());
    }

    @Test public void
    fallsBackToAMoreGeneralLanguageWhenCountrySpecificLanguageNotAvailable() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en-GB");
        assertThat("best locale", acceptLanguage.selectBest("en"), equalTo(ENGLISH));
    }
}