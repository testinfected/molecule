package com.vtence.molecule.session;

import java.time.Clock;
import java.time.Instant;

public class CookieSessionStore implements SessionStore {

    private final SessionIdentifierPolicy identifierPolicy;
    private final SessionEncoder encoder;

    private Clock clock = Clock.systemDefaultZone();
    private boolean renew;
    private int idleTimeout;
    private int timeToLive;

    public static CookieSessionStore secure(String key) {
        return new CookieSessionStore(new SecureIdentifierPolicy(), new SecureSessionEncoder(key));
    }

    public CookieSessionStore(SessionIdentifierPolicy policy, SessionEncoder encoder) {
        this.identifierPolicy = policy;
        this.encoder = encoder;
    }

    public CookieSessionStore renewIds() {
        this.renew = true;
        return this;
    }

    public CookieSessionStore idleTimeout(int seconds) {
        this.idleTimeout = seconds;
        return this;
    }

    public CookieSessionStore timeToLive(int seconds) {
        this.timeToLive = seconds;
        return this;
    }

    public CookieSessionStore usingClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public Session load(String id) throws Exception {
        Session session = encoder.decode(id);
        if (!validate(session)) return null;
        else return session;
    }

    public String save(Session data) throws Exception {
        String sid = sessionId(data);
        Session session = makeSession(data, sid);
        Instant now = now();
        session.updatedAt(now);
        if (!sid.equals(data.id())) {
            session.createdAt(now);
        }
        return encoder.encode(session);
    }

    public void destroy(String sid) {
        // nothing to do, it's stored on the client
    }

    private boolean validate(Session session) {
        if (expired(session) || stale(session) || dead(session)) session.invalidate();
        return !session.invalid();
    }

    private boolean expired(Session session) {
        return session.expires() && session.expired(now());
    }

    private boolean stale(Session session) {
        return !session.expires() && idleTimeout > 0 && !now().isBefore(staleTime(session));
    }

    private boolean dead(Session session) {
        return timeToLive > 0 && !now().isBefore(endOfLifeTime(session));
    }

    private Instant staleTime(Session session) {
        return session.updatedAt().plusSeconds(idleTimeout);
    }

    private Instant endOfLifeTime(Session session) {
        return session.updatedAt().plusSeconds(timeToLive);
    }

    private Instant now() {
        return clock.instant();
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
}