package com.vtence.molecule.session;

public interface SessionStore {

    Session load(String id);

    String save(Session session);

    void destroy(String sid);
}