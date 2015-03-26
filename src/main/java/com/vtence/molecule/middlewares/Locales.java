package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.AcceptLanguage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Locales extends AbstractMiddleware {

    private final List<String> supported;

    public Locales(String... supported) {
        this.supported = Arrays.asList(supported);
    }

    public void handle(Request request, Response response) throws Exception {
        AcceptLanguage acceptedLanguages = AcceptLanguage.of(request);
        Locale best = acceptedLanguages.selectBest(supported);
        request.attribute(Locale.class, best != null ? best : Locale.getDefault());
        forward(request, response);
    }
}