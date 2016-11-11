package com.vtence.molecule.http;

import com.vtence.molecule.Request;

import static com.vtence.molecule.http.HeaderNames.HOST;

public class Host {
    private final String hostname;
    private final Integer port;

    public static Host of(Request request) {
        String header = request.header(HOST);
        return header != null ? parse(header) : null;
    }

    public static Host parse(String header) {
        return new Host(parseName(header), parsePort(header));
    }

    public static String parseName(String header) {
        if (header.startsWith("[")) {
            return header.substring(1, header.indexOf(']'));
        }

        int colonIndex = header.indexOf(':');
        return colonIndex != -1 ? header.substring(0, colonIndex) : header;
    }

    public static int parsePort(String header) {
        int colonIndex = header.startsWith("[") ?
                header.indexOf(':', header.indexOf(']')) :
                header.indexOf(':');

        return colonIndex != -1 ? Integer.parseInt(header.substring(colonIndex + 1)) : -1;
    }

    public Host(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String name() {
        return hostname;
    }

    public int port(int defaultPort) {
        return port != -1 ? port : defaultPort;
    }
}
