package com.vtence.molecule.session;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionPool implements SessionStore, SessionHouse {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SessionIdentifierPolicy policy;

    private Clock clock = Clock.systemDefaultZone();
    private SessionPoolListener listener = SessionPoolListener.NONE;
    private int idleTimeout;
    private int timeToLive;
    private boolean renew;

    public static SessionPool secure() {
        return new SessionPool(new SecureIdentifierPolicy());
    }

    public SessionPool(SessionIdentifierPolicy policy) {
        this.policy = policy;
    }

    public SessionPool usingClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public SessionPool sessionListener(SessionPoolListener listener) {
        this.listener = listener;
        return this;
    }

    public SessionPool renewIds() {
        this.renew = true;
        return this;
    }

    public SessionPool idleTimeout(int seconds) {
        this.idleTimeout = seconds;
        return this;
    }

    public SessionPool timeToLive(int seconds) {
        this.timeToLive = seconds;
        return this;
    }

    public int size() {
        return sessions.size();
    }

    public Session load(String id) {
        Session session = sessions.get(id);
        if (session == null || !validate(session)) return null;
        Session data = makeSession(id, session);
        listener.sessionLoaded(id);
        return data;
    }

    public String save(Session data) {
        if (data.invalid()) throw new IllegalStateException("Session invalidated");
        if (shouldRenew(data)) destroy(data.id());

        String sid = sessionId(data);
        Session session = makeSession(sid, data);
        Instant now = now();
        session.updatedAt(now);
        sessions.put(sid, session);
        if (sid.equals(data.id())) {
            listener.sessionSaved(sid);
        } else {
            session.createdAt(now);
            listener.sessionCreated(sid);
        }
        return sid;
    }

    private boolean shouldRenew(Session data) {
        return !data.fresh() && renew;
    }

    public void destroy(String sid) {
        if (sessions.remove(sid) != null) listener.sessionDropped(sid);
    }

    public void clear() {
        sessions.clear();
    }

    public void houseKeeping() {
        sessions.values().stream().filter(session -> !validate(session)).forEach(session -> destroy(session.id()));
    }

    private String sessionId(Session data) {
        return data.fresh() || renew ? policy.generateId(data) : data.id();
    }

    private Session makeSession(String sid, Session data) {
        var session = new Session(sid);
        session.merge(data);
        session.maxAge(data.maxAge());
        session.updatedAt(data.updatedAt());
        session.createdAt(data.createdAt());
        return session;
    }

    private boolean validate(Session session) {
        if (expired(session) || stale(session) || tooOld(session)) session.invalidate();
        return !session.invalid();
    }

    private boolean expired(Session session) {
        return session.expires() && session.expired(now());
    }

    private boolean stale(Session session) {
        return !session.expires() && idleTimeout > 0 && !now().isBefore(staleTime(session));
    }

    private boolean tooOld(Session session) {
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
}
