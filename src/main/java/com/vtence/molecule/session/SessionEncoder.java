package com.vtence.molecule.session;

public interface SessionEncoder {

    String encode(Session data) throws Exception;

    Session decode(String content) throws Exception;
}