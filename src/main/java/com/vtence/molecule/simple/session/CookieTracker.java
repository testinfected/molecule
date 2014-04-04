package com.vtence.molecule.simple.session;

import com.vtence.molecule.Cookie;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.Session;

public class CookieTracker implements SessionTracker {

    private static final String STANDARD_SERVLET_SESSION_COOKIE = "JSESSIONID";

    private final SessionStore store;
    private final SessionIdentifierPolicy policy;
    private final String cookieName;

    public CookieTracker(SessionStore store) {
        this(store, new SecureIdentifierPolicy());
    }

    public CookieTracker(SessionStore store, SessionIdentifierPolicy policy) {
        this(store, policy, STANDARD_SERVLET_SESSION_COOKIE);
    }

    public CookieTracker(SessionStore store, SessionIdentifierPolicy policy, String cookieName) {
        this.store = store;
        this.policy = policy;
        this.cookieName = cookieName;
    }

    public Session acquireSession(Request request, Response response) {
        Cookie sessionCookie = request.cookie(cookieName);
        return sessionCookie != null ? store.load(sessionCookie.value()) : null;
    }

    public Session openSession(Request request, Response response) {
        Session session = store.create(policy.generateId());
        Cookie cookie = new Cookie(cookieName, session.id());
        cookie.httpOnly(true);
        response.add(cookie);
        return session;
    }
}
