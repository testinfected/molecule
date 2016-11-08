package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.vtence.molecule.http.HeaderNames.ACCEPT_ENCODING;
import static java.util.stream.Collectors.toList;

public class AcceptEncoding {
    private final List<Header.Value> values = new ArrayList<>();

    public static AcceptEncoding of(Request request) {
        String header = request.header(ACCEPT_ENCODING);
        return parse(header != null ? header : "");
    }

    public static AcceptEncoding parse(String header) {
        return from(new Header(header));
    }

    public static AcceptEncoding from(Header header) {
        AcceptEncoding accept = new AcceptEncoding();
        accept.values.addAll(header.all());
        return accept;
    }

    public String selectBestEncoding(String... candidates) {
        return selectBestEncoding(Arrays.asList(candidates));
    }

    public String selectBestEncoding(Collection<String> candidates) {
        List<Header.Value> contentCodings = explicitContentCodings(candidates);
        List<String> acceptableEncodings = filterAcceptable(contentCodings);
        for (String acceptable : acceptableEncodings) {
            if (candidates.contains(acceptable)) return acceptable;
        }
        return null;
    }

    private List<Header.Value> explicitContentCodings(Collection<String> availableEncodings) {
        List<Header.Value> codings = new ArrayList<>();

        for (Header.Value accept: values) {
            if (accept.is("*")) {
                List<String> others = new ArrayList<>(availableEncodings);
                others.removeAll(listValues(values));
                for (String other : others) {
                    codings.add(new Header.Value(other, accept.parameters()));
                }
            } else {
                codings.add(accept);
            }
        }

        return codings;
    }

    private List<String> filterAcceptable(List<Header.Value> encodings) {
        List<String> candidates = listValues(encodings);
        if (!candidates.contains("identity")) candidates.add("identity");

        for (Header.Value encoding : encodings) {
            if (!encoding.acceptable()) candidates.remove(encoding.value());
        }
        return candidates;
    }

    private List<String> listValues(List<Header.Value> entries) {
        return entries.stream().map(Header.Value::value).collect(toList());
    }
}
