package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.vtence.molecule.testing.RequestAssert.assertThat;

public class LocalesTest {

    Locale DEFAULT_LOCALE = Locale.US;
    Locale originalDefault = Locale.getDefault();

    Request request = new Request();
    Response response = new Response();

    @Before
    public void setPlatformDefaultLocale() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @After
    public void restPlatformDefaultLocale() {
        Locale.setDefault(originalDefault);
    }

    @Test
    public void usesThePlatformLocaleAsTheDefault() throws Exception {
        Locales locales = new Locales();
        locales.handle(request, response);

        assertThat(request).hasAttribute(Locale.class, DEFAULT_LOCALE);
    }

    @Test
    public void usesTheRequestedLanguageIfSupported() throws Exception {
        Locales locales = new Locales("en", "fr");
        locales.handle(request.header("Accept-Language", "fr"), response);

        assertThat(request).hasAttribute(Locale.class, Locale.FRENCH);
    }

    @Test
    public void usesTheHighestQualityLanguageSupported() throws Exception {
        Locales locales = new Locales("en", "fr");
        locales.handle(request.header("Accept-Language", "en; q=0.8, fr"), response);

        assertThat(request).hasAttribute(Locale.class, Locale.FRENCH);
    }

    @Test
    public void fallsBackToPlatformDefaultForUnsupportedLanguages() throws Exception {
        Locales locales = new Locales("en", "fr");
        locales.handle(request.header("Accept-Language", "es-ES"), response);

        assertThat(request).hasAttribute(Locale.class, DEFAULT_LOCALE);
    }

    @Test
    public void ignoresMalformedLanguageTags() throws Exception {
        Locales locales = new Locales("en", "fr");
        locales.handle(request.header("Accept-Language", "-fr-"), response);

        assertThat(request).hasAttribute(Locale.class, DEFAULT_LOCALE);
    }
}