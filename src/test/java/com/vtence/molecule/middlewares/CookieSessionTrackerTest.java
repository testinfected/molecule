package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.http.Cookie;
import com.vtence.molecule.lib.CookieJar;
import com.vtence.molecule.session.Session;
import com.vtence.molecule.session.SessionStore;
import org.hamcrest.FeatureMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.vtence.molecule.testing.CookieJarAssert.assertThat;
import static com.vtence.molecule.testing.RequestAssert.assertThat;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CookieSessionTrackerTest {

    @Rule public
    JUnitRuleMockery context = new JUnitRuleMockery();

    SessionStore store = context.mock(SessionStore.class);
    int timeout = (int) TimeUnit.MINUTES.toSeconds(30);
    String SESSION_COOKIE = CookieSessionTracker.STANDARD_SERVLET_SESSION_COOKIE;
    CookieSessionTracker tracker = new CookieSessionTracker(store).usingCookieName(SESSION_COOKIE);

    Request request = new Request();
    Response response = new Response();

    @Before public void
    stubSessionStore() {
        context.checking(new Expectations() {{
            allowing(store).load("existing"); will(returnValue(new Session("existing")));
            allowing(store).load("expired"); will(returnValue(null));
        }});
    }

    @Test(expected = IllegalStateException.class) public void
    requiresACookieJar() throws Exception {
        tracker.handle(request, response);
    }

    @Test public void
    createsSessionsForNewClientsButDoesNotCommitEmptySessions() throws Exception {
        CookieJar cookieJar = fillCookieJar();
        tracker.connectTo(echoSessionId());

        context.checking(new Expectations() {{
            never(store).save(with(any(Session.class)));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("Session: new");
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    createsSessionCookieOnceDone() throws Exception {
        CookieJar cookieJar = fillCookieJar();
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);

        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new").isHttpOnly();
    }

    @Test public void
    storesNewSessionIfNotEmpty() throws Exception {
        fillCookieJar();
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("Counter: 1");
    }

    @Test public void
    tracksExistingSessionsUsingACookieAndSavesSessionIfModified() throws Exception {
        fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));
        tracker.connectTo(incrementCounter());

        Session clientSession = store.load("existing");
        clientSession.put("counter", 1);
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("Counter: 2");
    }

    @Test public void
    savesExistingSessionEvenIfNotWritten() throws Exception {
        fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));

        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request, response);
        response.done();
        assertNoExecutionError();
    }

    @Test public void
    createsAFreshSessionIfClientSessionHasExpired() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "expired"));
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(response).hasBodyText("Counter: 1");
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new");
    }

    @Test public void
    doesNotSendTheSameSessionIdIfItDidNotChange() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));

        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    destroysInvalidSessions() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));
        tracker.connectTo(writeAndInvalidateSession());

        context.checking(new Expectations() {{
            oneOf(store).destroy(with("existing"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasDiscardedCookie(SESSION_COOKIE);
    }

    @Test public void
    usesPersistentSessionsByDefault() throws Exception {
        CookieJar cookieJar = fillCookieJar();
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(-1))); will(returnValue("persistent"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(-1);
    }

    @Test public void
    setsSessionAndCookieToExpireIfExpirationPeriodSpecified() throws Exception {
        CookieJar cookieJar = fillCookieJar();
        tracker.expireAfter(timeout);
        tracker.connectTo(incrementCounter());

        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(timeout))); will(returnValue("expires"));
        }});
        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    setsCookieToExpireAfterSessionMaxAge() throws Exception {
        CookieJar cookieJar = fillCookieJar();
        tracker.connectTo(expireSessionAfter(timeout));

        context.checking(new Expectations() {{
            allowing(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    refreshesCookieForExistingSessionsIfMaxAgeSpecified() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));
        tracker.connectTo(expireSessionAfter(timeout));

        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    ignoresDroppedSessions() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));
        tracker.connectTo(writeAndDropSession());

        context.checking(new Expectations() {{
            never(store).save(with(any(Session.class)));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    usesNewSessionIfRenewed() throws Exception {
        CookieJar cookieJar = fillCookieJar(new Cookie(SESSION_COOKIE, "existing"));
        tracker.connectTo(writeNewSession());

        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        tracker.handle(request, response);
        response.done();

        assertNoExecutionError();
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new");
    }

    @Test public void
    unbindsSessionAfterwards() throws Exception {
        fillCookieJar();
        tracker.handle(request, response);
        assertThat(request).hasAttribute(Session.class, notNullValue());

        response.done();
        assertNoExecutionError();
        assertThat(request).hasNoAttribute(Session.class);
    }

    @Test public void
    unbindsSessionInCaseOfErrorsToo() throws Exception {
        fillCookieJar();
        tracker.handle(request, response);

        response.done(new Exception("Error!"));
        assertThat(request).hasNoAttribute(Session.class);
    }

    private void assertNoExecutionError() throws ExecutionException, InterruptedException {
        response.await();
    }

    private CookieJar fillCookieJar(Cookie... cookies) {
        CookieJar cookieJar = new CookieJar(cookies);
        cookieJar.bind(request);
        return cookieJar;
    }

    private FeatureMatcher<Session, String> newSession() {
        return sessionWithId(null);
    }

    private FeatureMatcher<Session, String> sessionWithId(String sessionId) {
        return new FeatureMatcher<Session, String>(equalTo(sessionId), "session with id", "session id") {
            protected String featureValueOf(Session actual) {
                return actual.id();
            }
        };
    }

    private FeatureMatcher<Session, Integer> sessionWithMaxAge(final int maxAge) {
        return new FeatureMatcher<Session, Integer>(equalTo(maxAge), "session with max age", "max age") {
            protected Integer featureValueOf(Session actual) {
                return actual.maxAge();
            }
        };
    }

    private Application echoSessionId() {
        return (request, response) -> {
            Session session = Session.get(request);
            response.body("Session: " + (session.exists() ? session.id() : "new"));
        };
    }

    private Application incrementCounter() {
        return (request, response) -> {
            Session session = Session.get(request);
            Integer counter = session.contains("counter") ? session.get("counter") : 0;
            session.put("counter", counter++);
            response.body("Counter: " + counter);
        };
    }

    private Application expireSessionAfter(final int timeout) {
        return (request, response) -> {
            Session session = Session.get(request);
            session.put("written", true);
            session.maxAge(timeout);
        };
    }

    private Application writeAndInvalidateSession() {
        return (request, response) -> {
            Session session = Session.get(request);
            session.put("written", true);
            session.invalidate();
        };
    }

    private Application writeAndDropSession() {
        return (request, response) -> {
            Session session = Session.get(request);
            session.put("written", true);
            Session.unbind(request);
            response.done();
        };
    }

    private Application writeNewSession() {
        return (request, response) -> {
            Session session = new Session();
            session.put("written", true);
            session.bind(request);
            response.done();
        };
    }
}
