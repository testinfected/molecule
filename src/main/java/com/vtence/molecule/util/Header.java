package com.vtence.molecule.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Header {

    // Split on comma only if that comma has zero, or an even number of quotes in ahead of it.
    private static final Pattern ENTRIES_DELIMITER =
            Pattern.compile(",\\s*(?=([^\"]*\"[^\"]*\")*[^\"]*$)\\s*");
    private static final Pattern PARAMETERS_DELIMITER = Pattern.compile("\\s*;\\s*");
    private static final Pattern QUALITY = Pattern.compile("\\Aq=([\\d.]+)");

    private final List<Entry> entries;

    public static Header parse(String header) {
        return new Header(header);
    }

    protected Header(String header) {
        this.entries = sortByQuality(parseEntries(header));
    }

    public List<Entry> entries() {
        return new ArrayList<Entry>(entries);
    }

    public List<String> values() {
        List<String> values = new ArrayList<String>();
        for (Entry entry : entries) {
            if (entry.acceptable()) values.add(entry.value());
        }
        return values;
    }

    private List<Entry> parseEntries(String header) {
        List<Entry> entries = new ArrayList<Entry>();
        for (String entry : ENTRIES_DELIMITER.split(header)) {
            String[] parts = PARAMETERS_DELIMITER.split(entry, 2);
            String value = parts[0];
            String parameters = parts.length > 1 ? parts[1] : "";
            double quality = parseQuality(parameters);
            entries.add(new Entry(value, quality));
        }
        return entries;
    }

    private double parseQuality(String parameters) {
        Matcher matcher = QUALITY.matcher(parameters);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        } else {
            return 1.0;
        }
    }

    private List<Entry> sortByQuality(List<Entry> entries) {
        Collections.sort(entries);
        return entries;
    }

    public static class Entry implements Comparable<Entry> {
        private final String value;
        private final double quality;

        public Entry(String value, double quality) {
            this.value = value;
            this.quality = quality;
        }

        public String value() {
            return value;
        }

        public boolean is(String value) {
            return this.value.equals(value);
        }

        public double quality() {
            return quality;
        }

        public boolean acceptable() {
            return quality > 0;
        }

        public int compareTo(Entry other) {
            return Double.compare(other.quality, quality);
        }
    }
}
