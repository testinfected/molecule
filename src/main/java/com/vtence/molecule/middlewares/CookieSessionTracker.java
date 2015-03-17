package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.session.Session;
import com.vtence.molecule.session.SessionStore;

public class CookieSessionTracker extends AbstractMiddleware {

    private static final String STANDARD_SERVLET_SESSION_COOKIE = "JSESSIONID";

    private final SessionStore store;

    private String name = STANDARD_SERVLET_SESSION_COOKIE;
    private int expireAfter = -1;

    public CookieSessionTracker(SessionStore store) {
        this.store = store;
    }

    public CookieSessionTracker usingCookieName(String name) {
        this.name = name;
        return this;
    }

    public CookieSessionTracker expireAfter(int seconds) {
        this.expireAfter = seconds;
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        CookieJar cookieJar = CookieJar.get(request);
        if (cookieJar == null) throw new IllegalStateException("No cookie jar bound to request");
        Session session = openSession(cookieJar);
        session.bind(request);
        try {
            forward(request, response);
            commitSession(session, cookieJar);
        } finally {
            session.unbind(request);
        }
    }

    private Session openSession(CookieJar cookieJar) {
        Session session = acquireSession(cookieJar);
        session.maxAge(expireAfter);
        return session;
    }

    private Session acquireSession(CookieJar cookieJar) {
        String id = sessionId(cookieJar);
        if (id == null) return new Session();
        Session session = store.load(id);
        return session != null ? session : new Session();
    }

    private String sessionId(CookieJar cookieJar) {
        Cookie sessionCookie = cookieJar.get(name);
        return sessionCookie != null ? sessionCookie.value() : null;
    }

    private void commitSession(Session session, CookieJar cookieJar) {
        if (shouldDiscard(session)) {
            return;
        }

        if (session.invalid()) {
            destroy(session);
            cookieJar.discard(name);
            return;
        }

        String sid = save(session);
        if (newSession(sid, cookieJar) || expires(session)) {
            cookieJar.add(new Cookie(name, sid).httpOnly(true).maxAge(session.maxAge()));
        }
    }

    private boolean shouldDiscard(Session session) {
        return !session.exists() && session.isEmpty();
    }

    private boolean newSession(String sid, CookieJar cookieJar) {
        return !sid.equals(sessionId(cookieJar));
    }

    private boolean expires(Session session) {
        return session.maxAge() >= 0;
    }

    private void destroy(Session session) {
        store.destroy(session.id());
    }

    private String save(Session session) {
        return store.save(session);
    }
}