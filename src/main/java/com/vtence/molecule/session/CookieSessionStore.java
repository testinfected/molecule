package com.vtence.molecule.session;

public class CookieSessionStore implements SessionStore {

    private final SessionIdentifierPolicy identifierPolicy;
    private final SessionCookieEncoder coder;

    public CookieSessionStore(SessionIdentifierPolicy identifierPolicy, SessionCookieEncoder coder) {
        this.identifierPolicy = identifierPolicy;
        this.coder = coder;
    }

    public Session load(String id) {
        return coder.decode(id);
    }

    public String save(Session data) {
        String sid = data.fresh() ? identifierPolicy.generateId(data) : data.id();
        Session session = makeSession(data, sid);
        return coder.encode(session);
    }

    private Session makeSession(Session data, String sid) {
        Session session = new Session(sid);
        session.merge(data);
        session.maxAge(data.maxAge());
        session.updatedAt(data.updatedAt());
        session.createdAt(data.createdAt());
        return session;
    }

    public void destroy(String sid) {
        // nothing to do, it's stored on the client
    }
}