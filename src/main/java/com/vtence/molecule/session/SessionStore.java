package com.vtence.molecule.session;

public interface SessionStore {

    Session load(String id) throws Exception;

    String save(Session session) throws Exception;

    void destroy(String sid) throws Exception;
}