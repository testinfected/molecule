package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static java.util.Locale.FRENCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LocalesTest {

    Locale DEFAULT_LOCALE = Locale.US;
    Locale originalDefault = Locale.getDefault();
    Locale selected;

    Request request = new Request();
    Response response = new Response();

    @Before
    public void setPlatformDefaultLocale() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @After
    public void restoreDefaultPlatformLocale() {
        Locale.setDefault(originalDefault);
    }

    @Test
    public void usesThePlatformLocaleAsTheDefault() throws Exception {
        assertThat(selectPreferred(null, fromSupported()), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void usesTheRequestedLanguageIfSupported() throws Exception {
        assertThat(selectPreferred("fr", fromSupported("en", "fr")), equalTo(FRENCH));
    }

    @Test
    public void usesTheHighestQualityLanguageSupported() throws Exception {
        assertThat(selectPreferred("en; q=0.8, fr", fromSupported("en", "fr")), equalTo(FRENCH));
    }

    @Test
    public void fallsBackToPlatformDefaultForUnsupportedLanguages() throws Exception {
        assertThat(selectPreferred("es-ES", fromSupported("en", "fr")), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void ignoresMalformedLanguageTags() throws Exception {
        assertThat(selectPreferred("-fr-", fromSupported("en", "fr")), equalTo(DEFAULT_LOCALE));
    }

    @Test public void
    unbindsPreferredLocaleDone() throws Exception {
        Locales locales = new Locales();
        locales.handle(request, response);

        assertThat(request).hasNoAttribute(Locale.class);
    }

    private Locale selectPreferred(String accepted, String... supported) throws Exception {
        Locales locales = new Locales(supported);
        locales.connectTo(new Application() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                selected = request.attribute(Locale.class);
            }
        });
        locales.handle(request.header("Accept-Language", accepted), response);

        return selected;
    }

    private String[] fromSupported(String... supported) {
        return supported;
    }
}