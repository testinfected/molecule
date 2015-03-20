package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.ACCEPT_LANGUAGE;

public class Locales extends AbstractMiddleware {

    private final List<String> supported;

    public Locales(String... supported) {
        this.supported = Arrays.asList(supported);
    }

    public void handle(Request request, Response response) throws Exception {
        String acceptLanguage = request.header(ACCEPT_LANGUAGE);

        Locale best = null;

        if (acceptLanguage != null) {
            Locale candidate = Locale.forLanguageTag(acceptLanguage);
            if (supported.contains(candidate.toLanguageTag())) {
                best = candidate;
            }
        }

        request.attribute(Locale.class, best != null ? best : Locale.getDefault());
        forward(request, response);
    }
}
