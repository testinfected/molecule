package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.ACCEPT_LANGUAGE;

public class AcceptLanguage {
    private final List<Locale> locales = new ArrayList<Locale>();

    public static AcceptLanguage of(Request request) {
        String header = request.header(ACCEPT_LANGUAGE);
        return header != null ? new AcceptLanguage(header) : new AcceptLanguage("");
    }

    public AcceptLanguage(String header) {
        this(new Header(header));
    }

    public AcceptLanguage(Header header) {
        parseLocales(header);
    }

    private void parseLocales(Header header) {
        for (String locale : header.values()) {
            if (!locale.equals("")) locales.add(Locale.forLanguageTag(locale));
        }
    }

    public List<Locale> list() {
        return Collections.unmodifiableList(locales);
    }

    public Locale selectBest(String... supported) {
        return selectBest(Arrays.asList(supported));
    }

    public Locale selectBest(Collection<String> supported) {
        for (Locale candidate : list()) {
            if (supported.contains(candidate.toLanguageTag())) {
                return candidate;
            }
        }
        return null;
    }
}
