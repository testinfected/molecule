package com.vtence.molecule.middlewares;

import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.session.Session;
import com.vtence.molecule.session.SessionStore;

public class CookieSessionTracker extends AbstractMiddleware {

    public static final String STANDARD_SERVLET_SESSION_COOKIE = "JSESSIONID";

    private final SessionStore store;

    private String name = "molecule.session";
    private int expireAfter = -1;
    private FailureReporter failureReporter = FailureReporter.IGNORE;

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

    public void reportFailureTo(FailureReporter failureReporter) {
        this.failureReporter = failureReporter;
    }

    public void handle(Request request, Response response) throws Exception {
        CookieJar cookieJar = fetchCookieJar(request);
        acquireSession(cookieJar).bind(request);

        forward(request, response)
                .whenSuccessful(result -> commitSession(request))
                .whenComplete((error, action) -> Session.unbind(request));
    }

    private CookieJar fetchCookieJar(Request request) {
        CookieJar cookieJar = CookieJar.get(request);
        if (cookieJar == null) throw new IllegalStateException("No cookie jar bound to request");
        return cookieJar;
    }

    private Session acquireSession(CookieJar cookieJar) {
        String id = sessionId(cookieJar);
        if (id == null) return openSession();
        Session session = load(id);
        return session != null ? session : openSession();
    }

    private Session load(String id) {
        try {
            return store.load(id);
        } catch (Exception error) {
            failureReporter.errorOccurred(error);
            return null;
        }
    }

    private Session openSession() {
        Session session = new Session();
        session.maxAge(expireAfter);
        return session;
    }

    private String sessionId(CookieJar cookieJar) {
        Cookie sessionCookie = cookieJar.get(name);
        return sessionCookie != null ? sessionCookie.value() : null;
    }

    private void commitSession(Request request) {
        CookieJar cookieJar = fetchCookieJar(request);
        Session session = Session.get(request);

        if (shouldDiscard(session)) {
            return;
        }

        if (session.invalid()) {
            destroy(session);
            cookieJar.discard(name);
            return;
        }

        String sid = save(session);
        if (sid == null) {
            // session could not be saved, drop session content
            return;
        }

        if (sessionChanged(sid, cookieJar) || expires(session)) {
            cookieJar.add(new Cookie(name, sid).httpOnly(true).maxAge(session.maxAge()));
        }
    }

    private boolean shouldDiscard(Session session) {
        return session == null || !shouldUpdate(session);
    }

    private boolean shouldUpdate(Session session) {
        return !session.fresh() || !session.isEmpty();
    }

    private boolean sessionChanged(String sid, CookieJar cookieJar) {
        return !sid.equals(sessionId(cookieJar));
    }

    private boolean expires(Session session) {
        return session.expires();
    }

    private void destroy(Session session) {
        try {
            store.destroy(session.id());
        } catch (Exception error) {
            failureReporter.errorOccurred(error);
        }
    }

    private String save(Session session) {
        try {
            return store.save(session);
        } catch (Throwable error) {
            failureReporter.errorOccurred(error);
            return null;
        }
    }
}
