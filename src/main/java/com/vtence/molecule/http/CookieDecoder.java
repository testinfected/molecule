package com.vtence.molecule.http;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CookieDecoder {

    private static final Pattern NAME_VALUE_PAIR = Pattern.compile("(?:\\s|[;,])*([^;=]+)(?:=([\"]((?:\\\\\"|[^\"])*)[\"]|[^;,]*))?");

    private static final int NAME = 1;
    private static final int VALUE = 2;

    private static final String VERSION = "$Version";
    private static final String PATH = "$Path";
    private static final String DOMAIN = "$Domain";

    public List<Cookie> decode(String header) {
        List<Cookie> cookies = new ArrayList<>();

        Matcher pair = NAME_VALUE_PAIR.matcher(header);

        int pos = 0;
        int version = 1;

        if (pair.find()) {
            if (name(pair).equals(VERSION)) {
                pos = pair.end();
                version = version(pair);
            }
        }

        while (pair.find(pos)) {
            pos = pair.end();

            Cookie cookie = new Cookie(name(pair), value(pair));
            cookie.version(version);

            if (pair.find(pos)) {
                if (name(pair).equals(PATH)) {
                    pos = pair.end();
                    cookie.path(value(pair));
                }
            }

            if (pair.find(pos)) {
                if (name(pair).equals(DOMAIN)) {
                    pos = pair.end();
                    cookie.domain(value(pair));
                }
            }

            cookies.add(cookie);
        }
        return cookies;
    }

    private int version(Matcher pair) {
        try {
            return Integer.parseInt(value(pair));
        } catch (Exception e) {
            return 1;
        }
    }

    private String name(Matcher pair) {
        return pair.group(NAME);
    }

    private String value(Matcher pair) {
        return unescape(unquote(pair.group(VALUE)));
    }

    private String unescape(String text) {
        if (text == null) return null;

        return text.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String unquote(String text) {
        if (text == null) return null;

        if (text.length() > 2 && firstChar(text) == '"' && lastChar(text) == '"') {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private char lastChar(String text) {
        return text.charAt(text.length() - 1);
    }

    private char firstChar(String text) {
        return text.charAt(0);
    }
}
