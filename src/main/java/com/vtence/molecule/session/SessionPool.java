package com.vtence.molecule.session;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionPool implements SessionStore, SessionHouse {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SessionIdentifierPolicy policy;
    private final Clock clock;

    private SessionPoolListener listener = SessionPoolListener.NONE;
    private int idleTimeout = -1;

    public SessionPool() {
        this(new SecureIdentifierPolicy());
    }

    public SessionPool(SessionIdentifierPolicy policy) {
        this(policy, Clock.systemDefaultZone());
    }

    public SessionPool(SessionIdentifierPolicy policy, Clock clock) {
        this.policy = policy;
        this.clock = clock;
    }

    public SessionPool sessionListener(SessionPoolListener listener) {
        this.listener = listener;
        return this;
    }

    public SessionPool idleTimeout(int seconds) {
        this.idleTimeout = seconds;
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
        return data.exists() && contains(data.id()) ? data.id() : policy.generateId();
    }

    private Session makeSession(String sid, Session data) {
        Session session = new Session(sid);
        session.merge(data);
        session.maxAge(data.maxAge());
        session.updatedAt(data.updatedAt());
        session.createdAt(data.createdAt());
        return session;
    }

    private boolean contains(String id) {
        return sessions.containsKey(id);
    }

    private boolean validate(Session session) {
        if (expired(session) || stale(session)) session.invalidate();
        return !session.invalid();
    }

    private boolean expired(Session session) {
        return session.expires() && session.expired(now());
    }

    private boolean stale(Session session) {
        return !session.expires() && idleTimeout >= 0 && !now().isBefore(staleTime(session));
    }

    private Instant staleTime(Session session) {
        return session.updatedAt().plusSeconds(idleTimeout);
    }

    private Instant now() {
        return clock.instant();
    }
}
