package com.vtence.molecule.simple.session;

import com.vtence.molecule.Session;
import com.vtence.molecule.util.Clock;
import com.vtence.molecule.util.SystemClock;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SessionPool implements SessionStore, SessionHouse {

    private static final long HALF_AN_HOUR = TimeUnit.MINUTES.toSeconds(30);

    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    private final SessionIdentifierPolicy policy;
    private final Clock clock;
    private final long timeout;

    private SessionPoolListener listener = SessionPoolListener.NONE;

    public SessionPool() {
        this(HALF_AN_HOUR);
    }

    public SessionPool(SessionIdentifierPolicy policy, Clock clock) {
        this(policy, clock, -1);
    }

    public SessionPool(long timeoutInSeconds) {
        this(new SecureIdentifierPolicy(), new SystemClock(), timeoutInSeconds);
    }

    public SessionPool(SessionIdentifierPolicy policy, Clock clock, long timeout) {
        this.policy = policy;
        this.clock = clock;
        this.timeout = timeout;
    }

    public void setSessionListener(SessionPoolListener listener) {
        this.listener = listener;
    }

    public int size() {
        return sessions.size();
    }

    public Session get(String id) {
        Session session = removeIfInvalid(sessions.get(id));
        if (session == null) return null;
        session.updatedAt(clock.now());
        listener.sessionLoaded(id);
        return session;
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
        Date now = clock.now();
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
        for (Session session : sessions.values()) {
            if (!validate(session)) destroy(session.id());
        }
    }

    private String sessionId(Session data) {
        return data.exists() && contains(data.id()) ? data.id() : policy.generateId();
    }

    private Session makeSession(String sid, Session data) {
        Session session = new SessionHash(sid);
        session.merge(data);
        session.maxAge(data.maxAge());
        session.updatedAt(data.updatedAt());
        session.createdAt(data.createdAt());
        return session;
    }

    private boolean contains(String id) {
        return sessions.containsKey(id);
    }

    public Session create(String key) {
        Session session = new SessionHash(key);
        session.updatedAt(clock.now());
        session.maxAge((int) timeout);
        sessions.put(key, session);
        listener.sessionCreated(key);
        return session;
    }

    private boolean validate(Session session) {
        if (expired(session)) session.invalidate();
        return !session.invalid();
    }

    private Session removeIfInvalid(Session session) {
        if (session == null) return null;
        return !validate(session) ? remove(session) : session;
    }

    private boolean expired(Session session) {
        return session.expirationTime() != null && !clock.now().before(session.expirationTime());
    }

    private Session remove(Session session) {
        sessions.remove(session.id());
        listener.sessionDropped(session.id());
        return null;
    }
}
