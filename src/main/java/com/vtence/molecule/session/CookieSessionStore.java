package com.vtence.molecule.session;

public class CookieSessionStore implements SessionStore {

    private final SessionIdentifierPolicy identifierPolicy;
    private final SessionCookieEncoder coder;
    private boolean renew;

    public CookieSessionStore(SessionIdentifierPolicy policy, SessionCookieEncoder coder) {
        this.identifierPolicy = policy;
        this.coder = coder;
    }

    public void renewIds() {
        this.renew = true;
    }

    public Session load(String id) throws Exception {
        return coder.decode(id);
    }

    public String save(Session data) throws Exception {
        String sid = sessionId(data);
        Session session = makeSession(data, sid);
        return coder.encode(session);
    }

    private String sessionId(Session data) {
        return data.fresh() || renew ? identifierPolicy.generateId(data) : data.id();
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