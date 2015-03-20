package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.ACCEPT_LANGUAGE;

public class AcceptLanguage {
    private final List<Locale> locales = new ArrayList<Locale>();

    public static AcceptLanguage of(Request request) {
        String header = request.header(ACCEPT_LANGUAGE);
        return header != null ? new AcceptLanguage(header) : new AcceptLanguage("");
    }

    public AcceptLanguage(Header header) {
        parseLocales(header);
    }

    private void parseLocales(Header header) {
        for (String locale : header.values()) {
            if (!locale.equals("")) locales.add(Locale.forLanguageTag(locale));
        }
    }

    public AcceptLanguage(String header) {
        this(new Header(header));
    }

    public List<Locale> locales() {
        return new ArrayList<Locale>(locales);
    }
}
