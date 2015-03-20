package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static com.vtence.molecule.testing.RequestAssert.assertThat;

public class LocalesTest {

    Locale originalDefault = Locale.getDefault();

    Request request = new Request();
    Response response = new Response();

    @Before
    public void setPlatformDefaultLocale() {
        Locale.setDefault(Locale.US);
    }

    @After
    public void restPlatformDefaultLocale() {
        Locale.setDefault(originalDefault);
    }

    @Test
    public void usesThePlatformLocaleAsTheDefault() throws Exception {
        Locales locales = new Locales();
        locales.handle(request, response);
        assertThat(request).hasAttribute(Locale.class, Locale.US);
    }

}