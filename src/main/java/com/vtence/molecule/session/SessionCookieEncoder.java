package com.vtence.molecule.session;

public interface SessionCookieEncoder {

    String encode(Session data) throws Exception;

    Session decode(String encoded) throws Exception;
}