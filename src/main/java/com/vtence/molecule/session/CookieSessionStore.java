package com.vtence.molecule.session;

import java.time.Clock;
import java.time.Instant;

public class CookieSessionStore implements SessionStore {

    private final SessionIdentifierPolicy identifierPolicy;
    private final SessionCookieEncoder encoder;
    private final Clock clock;
    private boolean renew;

    public CookieSessionStore(SessionIdentifierPolicy policy, SessionCookieEncoder encoder) {
        this(policy, encoder, Clock.systemDefaultZone());
    }

    public CookieSessionStore(SessionIdentifierPolicy policy, SessionCookieEncoder encoder, Clock clock) {
        this.identifierPolicy = policy;
        this.encoder = encoder;
        this.clock = clock;
    }

    public void renewIds() {
        this.renew = true;
    }

    public Session load(String id) throws Exception {
        return encoder.decode(id);
    }

    public String save(Session data) throws Exception {
        String sid = sessionId(data);
        Session session = makeSession(data, sid);
        Instant now = clock.instant();
        session.updatedAt(now);
        if (!sid.equals(data.id())) {
            session.createdAt(now);
        }
        return encoder.encode(session);
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