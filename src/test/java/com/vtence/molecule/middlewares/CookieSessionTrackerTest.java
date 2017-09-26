package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.FailureReporter;
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
    FailureReporter failureReporter = context.mock(FailureReporter.class);
    int timeout = (int) TimeUnit.MINUTES.toSeconds(30);
    String SESSION_COOKIE = CookieSessionTracker.STANDARD_SERVLET_SESSION_COOKIE;
    CookieSessionTracker tracker = new CookieSessionTracker(store).usingCookieName(SESSION_COOKIE);

    @Before public void
    stubSessionStore() throws Exception {
        context.checking(new Expectations() {{
            allowing(store).load("existing"); will(returnValue(new Session("existing")));
            allowing(store).load("expired"); will(returnValue(null));
        }});
    }

    @Test(expected = IllegalStateException.class) public void
    requiresACookieJar() throws Exception {
        tracker.then(ok()).handle(Request.get("/"));
    }

    @Test public void
    createsSessionsForNewClientsButDoesNotCommitEmptySessions() throws Exception {
        context.checking(new Expectations() {{
            never(store).save(with(any(Session.class)));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(echoSessionId()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("Session: new");
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    createsSessionCookieOnceDone() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(incrementCounter()).handle(request);
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);

        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new").isHttpOnly();
    }

    @Test public void
    storesNewSessionIfNotEmpty() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        Request request = Request.get("/");
        bindCookieJar(request);

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("Counter: 1");
    }

    @Test public void
    tracksExistingSessionsUsingACookieAndSavesSessionIfModified() throws Exception {
        Session clientSession = store.load("existing");
        clientSession.put("counter", 1);
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        Request request = Request.get("/");
        bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("Counter: 2");
    }

    @Test public void
    savesExistingSessionEvenIfNotWritten() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        Request request = Request.get("/");
        bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(ok()).handle(request);
        response.done();
        assertNoExecutionError(response);
    }

    @Test public void
    createsAFreshSessionIfClientSessionHasExpired() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "expired"));

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("Counter: 1");
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new");
    }

    @Test public void
    doesNotSendTheSameSessionIdIfItDidNotChange() throws Exception {
        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(ok()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    destroysInvalidSessions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).destroy(with("existing"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(writeAndInvalidateSession()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasDiscardedCookie(SESSION_COOKIE);
    }

    @Test public void
    usesPersistentSessionsByDefault() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(-1))); will(returnValue("persistent"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(-1);
    }

    @Test public void
    setsSessionAndCookieToExpireIfExpirationPeriodSpecified() throws Exception {
        tracker.expireAfter(timeout);

        context.checking(new Expectations() {{
            oneOf(store).save(with(sessionWithMaxAge(timeout))); will(returnValue("expires"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    setsCookieToExpireAfterSessionMaxAge() throws Exception {
        context.checking(new Expectations() {{
            allowing(store).save(with(newSession())); will(returnValue("new"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(expireSessionAfter(timeout)).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    refreshesCookieForExistingSessionsIfMaxAgeSpecified() throws Exception {
        context.checking(new Expectations() {{
            allowing(store).save(with(sessionWithId("existing"))); will(returnValue("existing"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(expireSessionAfter(timeout)).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasMaxAge(timeout);
    }

    @Test public void
    ignoresDroppedSessions() throws Exception {
        context.checking(new Expectations() {{
            never(store).save(with(any(Session.class)));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(writeAndDropSession()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    dropsContentOfCorruptedSessions() throws Exception {
        tracker.reportFailureTo(failureReporter);

        Exception saveError = new Exception("Save failed!");
        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(saveError);
            oneOf(store).save(with(any(Session.class))); will(throwException(saveError));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request);

        Response response = tracker.then(incrementCounter()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasNoCookie(SESSION_COOKIE);
    }

    @Test public void
    createsAFreshSessionIfClientSessionIsCorrupted() throws Exception {
        tracker.reportFailureTo(failureReporter);

        Exception loadError = new Exception("load failed!");
        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(loadError);
            allowing(store).load(with("corrupted")); will(throwException(loadError));
        }});

        Request request = Request.get("/");
        bindCookieJar(request, new Cookie(SESSION_COOKIE, "corrupted"));

        Response response = tracker.then(echoSessionId()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(response).hasBodyText("Session: new");
    }

    @Test public void
    reportsWhenSessionDestructionFails() throws Exception {
        tracker.reportFailureTo(failureReporter);

        Exception destroyError = new Exception("destroy failed!");
        context.checking(new Expectations() {{
            oneOf(failureReporter).errorOccurred(destroyError);
            allowing(store).destroy(with("existing")); will(throwException(destroyError));
        }});

        Request request = Request.get("/");
        bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(writeAndInvalidateSession()).handle(request);
        response.done();

        assertNoExecutionError(response);
    }

    @Test public void
    usesNewSessionIfRenewed() throws Exception {
        context.checking(new Expectations() {{
            oneOf(store).save(with(newSession())); will(returnValue("new"));
        }});

        Request request = Request.get("/");
        CookieJar cookieJar = bindCookieJar(request, new Cookie(SESSION_COOKIE, "existing"));

        Response response = tracker.then(writeNewSession()).handle(request);
        response.done();

        assertNoExecutionError(response);
        assertThat(cookieJar).hasCookie(SESSION_COOKIE).hasValue("new");
    }

    @Test public void
    unbindsSessionAfterwards() throws Exception {
        Request request = Request.get("/");
        bindCookieJar(request);

        Response response = tracker.then(ok()).handle(request);
        assertThat(request).hasAttribute(Session.class, notNullValue());

        response.done();
        assertNoExecutionError(response);
        assertThat(request).hasNoAttribute(Session.class);
    }

    @Test public void
    unbindsSessionInCaseOfErrorsToo() throws Exception {
        Request request = Request.get("/");
        bindCookieJar(request).bind(request);

        Response response = tracker.then(ok()).handle(request);

        response.done(new Exception("Error!"));
        assertThat(request).hasNoAttribute(Session.class);
    }

    private Application ok() {
        return request -> Response.ok();
    }

    private void assertNoExecutionError(Response response) throws ExecutionException, InterruptedException {
        response.await();
    }

    private CookieJar bindCookieJar(Request request, Cookie... cookies) {
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
        return request -> {
            Session session = Session.get(request);
            return Response.ok().body("Session: " + (session.fresh() ? "new" : session.id()));
        };
    }

    private Application incrementCounter() {
        return request -> {
            Session session = Session.get(request);
            Integer counter = session.contains("counter") ? session.get("counter") : 0;
            session.put("counter", counter++);
            return Response.ok().body("Counter: " + counter);
        };
    }

    private Application writeAndInvalidateSession() {
        return request -> {
            Session session = Session.get(request);
            session.put("written", true);
            session.invalidate();
            return Response.ok();
        };
    }

    private Application expireSessionAfter(final int timeout) {
        return request -> {
            Session session = Session.get(request);
            session.put("written", true);
            session.maxAge(timeout);
            return Response.ok();
        };
    }

    private Application writeAndDropSession() {
        return request -> {
            Session session = Session.get(request);
            session.put("written", true);
            Session.unbind(request);
            return Response.ok();
        };
    }

    private Application writeNewSession() {
        return request -> {
            Session session = new Session();
            session.put("written", true);
            session.bind(request);
            return Response.ok();
        };
    }
}
