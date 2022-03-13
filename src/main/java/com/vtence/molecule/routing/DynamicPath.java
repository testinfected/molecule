package com.vtence.molecule.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DynamicPath implements WithBoundParameters, Predicate<String> {

    private final Path pattern;
    private final boolean fullMatch;

    public DynamicPath(String pattern) {
        this(pattern, false);
    }

    public DynamicPath(String pattern, boolean fullMatch) {
        this.pattern = new Path(pattern);
        this.fullMatch = fullMatch;
    }

    public static DynamicPath equalTo(String pattern) {
        return new DynamicPath(pattern, true);
    }

    public static DynamicPath startingWith(String pattern) {
        return new DynamicPath(pattern, false);
    }

    public boolean test(String actual) {
        var path = new Path(actual);
        if (pattern.longerThan(path)) return false;
        if (fullMatch && path.longerThan(pattern)) return false;

        for (int i = 0; i < pattern.segmentCount(); i++) {
            if (!isDynamic(pattern.segment(i)) && !pattern.segment(i).equals(path.segment(i)))
                return false;
        }
        return true;
    }

    private boolean isDynamic(String segment) {
        return segment.startsWith(":");
    }

    public Map<String, String> parametersBoundTo(String path) {
        var p = new Path(path);
        var boundParameters = new HashMap<String, String>();

        for (int i = 0; i < pattern.segmentCount(); i++) {
            String segment = pattern.segment(i);
            if (isDynamic(segment)) {
                boundParameters.put(stripLeadingColon(segment), p.segment(i));
            }
        }
        return boundParameters;
    }

    private String stripLeadingColon(String segment) {
        return segment.substring(1);
    }

    public static class Path {
        private final String path;

        public Path(String path) {
            this.path = path;
        }

        public String[] segments() {
            return removeEmptyParts(path.split("/"));
        }

        private static String[] removeEmptyParts(String[] parts) {
            var segments = new ArrayList<String>();
            for (String part : parts) {
                if (!part.isEmpty()) segments.add(part);
            }
            return segments.toArray(new String[0]);
        }

        public boolean longerThan(Path other) {
            return segments().length > other.segments().length;
        }

        public String segment(int index) {
            return segments()[index];
        }

        public int segmentCount() {
            return segments().length;
        }
    }
}