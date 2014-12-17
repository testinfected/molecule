package com.vtence.molecule.http;

import java.util.Locale;

public class LanguageTag {

    public static Locale parse(String locale) {
        String[] parts = locale.split("-");
        String language = parts[0];
        String country = parts.length > 1 ? parts[1] : "";
        return new Locale(language, country);
    }

    public static String format(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return language.toLowerCase() + (country.equals("") ? "" : "-" + country.toLowerCase());
    }

    LanguageTag() {}
}
