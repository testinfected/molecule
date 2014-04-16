package com.vtence.molecule.simple.session;

import com.vtence.molecule.Session;

public interface SessionStore {

    Session load(String id);

    String save(Session session);

    void destroy(String sid);

    // todo remove
    Session create(String key);

    // todo remove
    Session get(String id);
}
