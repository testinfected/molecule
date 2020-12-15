package com.vtence.molecule.helpers;

import java.util.StringJoiner;

public class Joiner {

    private final String separator;

    public static Joiner on(String separator) {
        return new Joiner(separator);
    }

    public Joiner(String separator) {
        this.separator = separator;
    }

    public String join(Iterable<?> parts) {
        StringJoiner joiner = new StringJoiner(separator);
        for (Object part: parts) {
            joiner.add(String.valueOf(part));
        }
        return joiner.toString();
    }
}
