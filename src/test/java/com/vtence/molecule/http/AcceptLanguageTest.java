package com.vtence.molecule.http;

import org.junit.Test;

import static java.util.Locale.CANADA_FRENCH;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static java.util.Locale.ITALY;
import static java.util.Locale.UK;
import static java.util.Locale.US;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class AcceptLanguageTest {

    @Test public void
    prefersNoLocaleWhenEmpty() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("");
        assertThat("no locale", acceptLanguage.list(), empty());
    }

    @Test public void
    parsesNaturalLanguage() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("fr");
        assertThat("all locales", acceptLanguage.list(), contains(FRENCH));
    }

    @Test public void
    parsesNaturalLanguageAndCountry() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("fr-ca");
        assertThat("all locales", acceptLanguage.list(), contains(CANADA_FRENCH));
    }

    @Test public void
    listsAcceptableLocalesInPreferenceOrder() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en; q=0.8, fr-ca");
        assertThat("all locales", acceptLanguage.list(), contains(CANADA_FRENCH, ENGLISH));
    }

    @Test public void
    selectsNoLanguageWhenThereIsNoCandidate() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en");
        assertThat("best locale", acceptLanguage.selectBest(), nullValue());
    }

    @Test
    public void
    selectsAcceptableLanguageWithHighestQuality() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en; q=0.8, fr-ca");
        assertThat("best locale", acceptLanguage.selectBest(ENGLISH, CANADA_FRENCH), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsFirstAmongstSupportedLocalesOfSameQuality() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en; q=0.8, fr-ca, it-it");
        assertThat("best locale", acceptLanguage.selectBest(CANADA_FRENCH, ITALY), equalTo(CANADA_FRENCH));
    }

    @Test public void
    selectsNoLocaleWhenCandidatesAreNotAcceptable() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en; q=0, fr-ca");
        assertThat("best locale", acceptLanguage.selectBest(ENGLISH), nullValue());
    }

    @Test public void
    fallsBackToAMoreGeneralLanguageWhenCountrySpecificLanguageNotAvailable() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en-GB");
        assertThat("best locale", acceptLanguage.selectBest(US, ENGLISH), equalTo(ENGLISH));
    }

    @Test public void
    usesCountrySpecificLanguageWhenGeneralLanguageNotSupported() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en");
        assertThat("best locale", acceptLanguage.selectBest(FRENCH, UK), equalTo(UK));
    }

    @Test public void
    usesCountrySpecificLanguageEvenWhenThereIsALowerQualityExactMatch() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("fr; q=0.8, en");
        assertThat("best locale", acceptLanguage.selectBest(FRENCH, UK), equalTo(UK));
    }

    @Test public void
    fallbacksToAnotherCountryOfSameLanguageIfRequestedCountryIsNotAvailable() {
        AcceptLanguage acceptLanguage = AcceptLanguage.parse("en-GB");
        assertThat("best locale", acceptLanguage.selectBest(US), equalTo(US));
    }
}