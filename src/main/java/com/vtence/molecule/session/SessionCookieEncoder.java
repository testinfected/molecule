package com.vtence.molecule.session;

public interface SessionCookieEncoder {

    String encode(Session data);

    Session decode(String encoded);
}