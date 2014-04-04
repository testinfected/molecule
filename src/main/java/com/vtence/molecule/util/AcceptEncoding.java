package com.vtence.molecule.util;

import com.vtence.molecule.HttpHeaders;
import com.vtence.molecule.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AcceptEncoding {
    private final Header header;

    public static AcceptEncoding of(Request request) {
        return new AcceptEncoding(request.header(HttpHeaders.ACCEPT_ENCODING));
    }

    public AcceptEncoding(String header) {
        this(new Header(header));
    }

    public AcceptEncoding(Header header) {
        this.header = header;
    }

    public String selectBestEncoding(String... candidates) {
        return selectBestEncoding(Arrays.asList(candidates));
    }

    public String selectBestEncoding(Collection<String> candidates) {
        List<Header.Entry> contentCodings = explicitContentCodings(candidates);
        List<String> acceptableEncodings = listAcceptable(contentCodings);
        for (String acceptable : acceptableEncodings) {
            if (candidates.contains(acceptable)) return acceptable;
        }
        return null;
    }

    private List<Header.Entry> explicitContentCodings(Collection<String> availableEncodings) {
        List<Header.Entry> codings = new ArrayList<Header.Entry>();

        for (Header.Entry accept: header.entries()) {
            if (accept.is("*")) {
                List<String> others = new ArrayList<String>(availableEncodings);
                others.removeAll(listValues(header.entries()));
                for (String other : others) {
                    codings.add(new Header.Entry(other, accept.quality()));
                }
            } else {
                codings.add(accept);
            }
        }

        return codings;
    }

    private List<String> listAcceptable(List<Header.Entry> encodings) {
        List<String> candidates = listValues(encodings);
        if (!candidates.contains("identity")) candidates.add("identity");

        for (Header.Entry encoding : encodings) {
            if (!encoding.acceptable()) candidates.remove(encoding.value());
        }
        return candidates;
    }

    private List<String> listValues(List<Header.Entry> entries) {
        List<String> values = new ArrayList<String>();
        for (Header.Entry entry: entries) {
            values.add(entry.value());
        }
        return values;
    }
}
