package com.vtence.molecule.http;

import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Joiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LANGUAGE;
import static java.util.stream.Collectors.toList;

public class ContentLanguage {
    private final List<Locale> locales = new ArrayList<>();

    public static ContentLanguage of(Response response) {
        String header = response.header(CONTENT_LANGUAGE);
        return header != null ? ContentLanguage.parse(header) : null;
    }

    public static ContentLanguage parse(String header) {
        return from(new Header(header));
    }

    public static ContentLanguage from(Header header) {
        ContentLanguage languages = new ContentLanguage();
        header.values().stream().filter(locale -> !locale.equals(""))
              .forEach(locale -> languages.add(Locale.forLanguageTag(locale)));
        return languages;
    }

    public ContentLanguage add(Locale locale) {
        this.locales.add(locale);
        return this;
    }

    public List<Locale> locales() {
        return new ArrayList<>(locales);
    }

    public ContentLanguage remove(Locale locale) {
        locales.remove(locale);
        return this;
    }

    public String toString() {
        return Joiner.on(", ").join(format(locales));
    }

    private Iterable<String> format(Collection<Locale> locales) {
        return locales.stream().map(Locale::toLanguageTag).collect(toList());
    }
}
