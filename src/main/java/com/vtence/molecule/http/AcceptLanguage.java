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
    private final List<Locale> locales = new ArrayList<>();

    public static AcceptLanguage of(Request request) {
        String header = request.header(ACCEPT_LANGUAGE);
        return parse(header != null ? header : "");
    }

    public static AcceptLanguage parse(String header) {
        return AcceptLanguage.from(new Header(header != null ? header : ""));
    }

    public static AcceptLanguage from(Header header) {
        AcceptLanguage accept = new AcceptLanguage();
        header.values().stream()
              .filter(locale -> !locale.equals(""))
              .map(Locale::forLanguageTag).forEach(accept::add);
        return accept;
    }

    private void add(Locale locale) {
        this.locales.add(locale);
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
