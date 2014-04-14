package com.vtence.molecule.simple.session;

import com.vtence.molecule.Session;

public interface SessionStore {

    // todo remove
    Session create(String key);

    Session load(String id);

    String save(Session session); // return data

    void destroy(String id);
}
