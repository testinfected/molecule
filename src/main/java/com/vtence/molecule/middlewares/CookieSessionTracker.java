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
    private String sessionCookieName;

    public CookieSessionTracker(SessionStore store) {
        this(store, STANDARD_SERVLET_SESSION_COOKIE);
    }

    public CookieSessionTracker(SessionStore store, String cookieName) {
        this.store = store;
        this.sessionCookieName = cookieName;
    }

    public void handle(Request request, Response response) throws Exception {
        prepareSession(request);
        forward(request, response);
        commitSession(request, response);
    }

    private void prepareSession(Request request) {
        Session session = acquireSession(request);
        request.attribute(Session.class, session);
    }

    private Session acquireSession(Request request) {
        String id = sessionId(request);
        Session session = null;
        if (id != null) session = store.load(id);
        if (session == null) session = new SessionHash(null);
        return session;
    }

    private String sessionId(Request request) {
        return request.cookieValue(sessionCookieName);
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
        String data = save(session);
        if (newSession(request, data)) {
            setSessionCookie(response, data);
        }
    }

    private boolean shouldCommit(Session session) {
        return !session.isNew() || !session.isEmpty();
    }

    private void destroy(Session session) {
        store.destroy(session.id());
    }

    private String save(Session session) {
        return store.save(session);
    }

    private boolean newSession(Request request, String data) {
        return !data.equals(sessionId(request));
    }

    private void setSessionCookie(Response response, String data) {
        response.add(new Cookie(sessionCookieName, data).httpOnly(true));
    }
}