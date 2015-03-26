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
        assertThat("best locale", acceptLanguage.selectBest(ENGLISH, CANADA_FRENCH), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsFirstAmongstSupportedLocalesOfSameQuality() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0.8, fr-ca, it-it");
        assertThat("best locale", acceptLanguage.selectBest(CANADA_FRENCH, ITALY), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsNoLocaleWhenCandidatesAreNotAcceptable() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en; q=0, fr-ca");
        assertThat("best locale", acceptLanguage.selectBest(ENGLISH), nullValue());
    }

    @Test public void
    fallsBackToAMoreGeneralLanguageWhenCountrySpecificLanguageNotAvailable() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en-GB");
        assertThat("best locale", acceptLanguage.selectBest(US, ENGLISH), equalTo(ENGLISH));
    }

    @Test public void
    usesCountrySpecificLanguageWhenGeneralLanguageNotSupported() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en");
        assertThat("best locale", acceptLanguage.selectBest(FRENCH, UK), equalTo(UK));
    }

    @Test public void
    usesCountrySpecificLanguageEvenWhenThereIsALowerQualityExactMatch() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("fr; q=0.8, en");
        assertThat("best locale", acceptLanguage.selectBest(FRENCH, UK), equalTo(UK));
    }

    @Test public void
    fallbacksToAnotherCountryOfSameLanguageIfRequestedCountryIsNotAvailable() {
        AcceptLanguage acceptLanguage = new AcceptLanguage("en-GB");
        assertThat("best locale", acceptLanguage.selectBest(US), equalTo(US));
    }
}