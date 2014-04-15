package com.vtence.molecule.middlewares;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;
import com.vtence.molecule.simple.session.SessionHash;
import com.vtence.molecule.simple.session.SessionStore;

public class CookieSessionTracker extends AbstractMiddleware {

    private static final String STANDARD_SERVLET_SESSION_COOKIE = "JSESSIONID";

    private final SessionStore store;

    private String name = STANDARD_SERVLET_SESSION_COOKIE;
    private int expireAfter = -1;

    public CookieSessionTracker(SessionStore store) {
        this.store = store;
    }

    public CookieSessionTracker cookie(String name) {
        this.name = name;
        return this;
    }

    public CookieSessionTracker expireAfter(int seconds) {
        this.expireAfter = seconds;
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        prepareSession(request);
        forward(request, response);
        commitSession(request, response);
    }

    private void prepareSession(Request request) {
        Session session = acquireSession(request);
        request.attribute(Session.class, session);
        session.maxAge(expireAfter);
    }

    private Session acquireSession(Request request) {
        String id = sessionId(request);
        Session session = null;
        if (id != null) session = store.load(id);
        if (session == null) session = new SessionHash(null);
        return session;
    }

    private String sessionId(Request request) {
        return request.cookieValue(name);
    }

    private void commitSession(Request request, Response response) {
        Session session = request.attribute(Session.class);
        if (!shouldCommit(session)) {
            return;
        }
        if (session.invalid()) {
            destroy(session);
            return;
        }
        String sid = save(session);
        if (newSession(request, sid) || expires(session)) {
            Cookie cookie = new Cookie(name, sid);
            cookie.httpOnly(true);
            cookie.maxAge(session.maxAge());
            response.add(cookie);
        }
    }

    private boolean shouldCommit(Session session) {
        return !session.isNew() || !session.isEmpty();
    }

    private boolean newSession(Request request, String sid) {
        return !sid.equals(sessionId(request));
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
