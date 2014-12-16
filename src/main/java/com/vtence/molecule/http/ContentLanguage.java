package com.vtence.molecule.http;

import com.vtence.molecule.Response;
import com.vtence.molecule.helpers.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vtence.molecule.http.HeaderNames.CONTENT_LANGUAGE;

public class ContentLanguage {
    private final List<Locale> locales = new ArrayList<Locale>();

    public static ContentLanguage of(Response response) {
        return ContentLanguage.parse(response.get(CONTENT_LANGUAGE));
    }

    public static ContentLanguage parse(String header) {
        return header != null ? from(new Header(header)) : from(new Header(""));
    }

    public static ContentLanguage from(Header header) {
        ContentLanguage languages = new ContentLanguage();
        for (String locale : header.values()) {
            if (!locale.equals("")) languages.add(LanguageTag.parse(locale));
        }
        return languages;
    }

    public ContentLanguage add(Locale locale) {
        this.locales.add(locale);
        return this;
    }

    public List<Locale> locales() {
        return new ArrayList<Locale>(locales);
    }

    public ContentLanguage remove(Locale locale) {
        locales.remove(locale);
        return this;
    }

    public String toString() {
        return Joiner.on(", ").join(format(locales));
    }

    private Iterable<String> format(Iterable<Locale> locales) {
        List<String> formats = new ArrayList<String>();
        for (Locale locale : locales) {
            formats.add(LanguageTag.format(locale));
        }
        return formats;
    }
}
