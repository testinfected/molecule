package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import java.util.*;

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

    public Locale selectBest(Locale... candidates) {
        return selectBest(Arrays.asList(candidates));
    }

    public Locale selectBest(Collection<Locale> candidates) {
        for (Locale accepted : list()) {
            if (candidates.contains(accepted)) return accepted;
            if (candidates.contains(languageOf(accepted))) return languageOf(accepted);
            for (Locale candidate : candidates) {
                if (sameLanguage(accepted, candidate)) return candidate;
            }
        }
        return null;
    }

    private boolean sameLanguage(Locale accepted, Locale candidate) {
        return candidate.getLanguage().equals(accepted.getLanguage());
    }

    private Locale languageOf(Locale accepted) {
        return new Locale(accepted.getLanguage());
    }
}
