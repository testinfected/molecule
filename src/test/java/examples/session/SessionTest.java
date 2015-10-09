package examples.session;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.Delorean;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import com.vtence.molecule.testing.http.UrlEncodedForm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SessionTest {

    Delorean delorean = new Delorean();
    SessionExample sessions = new SessionExample(delorean);
    WebServer server = WebServer.create(9999);

    Throwable error;
    String SESSION_COOKIE = "JSESSIONID"; // The default session cookie name is the standard servlet cookie
    int FIVE_MIN = 300;

    HttpRequest request = new HttpRequest(9999);
    HttpResponse response;

    @Before
    public void startServer() throws IOException {
        sessions.run(server);
        server.failureReporter(e -> error = e);
    }

    @After
    public void stopServer() throws IOException {
        delorean.back();
        server.stop();
    }

    @Test
    public void accessingTheSessionWithoutCreatingANewSession() throws IOException {
        response = request.get("/");
        assertNoError();
        assertThat(response).hasNoCookie(SESSION_COOKIE)
                            .hasBodyText("Hello, Guest");
    }

    @Test
    public void trackingASessionAcrossRequests() throws IOException {
        response = request.but().content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        response = request.but().cookie(SESSION_COOKIE, sessionId).get("/");
        assertNoError();
        assertThat(response).hasBodyText("Hello, Vincent");
    }

    @Test
    public void creatingATransientSessionCookie() throws IOException {
        response = request.content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(-1);
    }

    @Test
    public void noteThatTransientSessionCookiesAreNotRefreshed() throws IOException {
        response = request.content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        // Play the same request again and include the cookie...
        response = request.cookie(SESSION_COOKIE, sessionId).send();
        assertNoError();
        // ... there will be no cookie this time
        assertThat(response).hasNoCookie(SESSION_COOKIE);
    }

    @Test
    public void creatingAPersistentSessionCookieWhichExpiresAfterFiveMinutes() throws IOException {
        response = request.content(new UrlEncodedForm().addField("username", "Vincent")
                                                       .addField("remember_me", "true"))
                          .post("/login");
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(FIVE_MIN);
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        // Play the same request again and include the cookie
        response = request.cookie(SESSION_COOKIE, sessionId).send();
        assertNoError();
        // ... the cookie will have been refreshed
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(FIVE_MIN);
    }

    @Test
    public void deletingASession() throws IOException {
        response = request.but().content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE);
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        response = request.but().cookie(SESSION_COOKIE, sessionId).delete("/logout");
        assertNoError();
        // Session cookie should be expired
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(0);

        response = request.but().get("/");
        assertNoError();
        // Back to being a guest
        assertThat(response).hasBodyText("Hello, Guest");
    }

    @Test
    public void
    attemptingToUseAnExpiredSession() throws Exception {
        response = request.but().content(new UrlEncodedForm().addField("username", "Vincent")
                                                             .addField("remember_me", "true"))
                          .post("/login");
        assertNoError();
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        delorean.travelInTime(SECONDS.toMillis(FIVE_MIN));

        response = request.but().cookie(SESSION_COOKIE, sessionId).get("/");
        assertNoError();
        assertThat(response).hasNoCookie(SESSION_COOKIE)
                            .hasBodyText("Hello, Guest");
    }

    @Test
    public void
    renewingASessionToAvoidSessionFixation() throws Exception {
        response = request.but().content(new UrlEncodedForm().addField("username", "attacker")).post("/login");
        assertNoError();
        String fixatedSession = response.cookie(SESSION_COOKIE).getValue();

        response = request.but().cookie(SESSION_COOKIE, fixatedSession)
                          .content(new UrlEncodedForm().addField("username", "Vincent")
                                                       .addField("remember_me", "true")
                                                       .addField("renew", "true"))
                          .post("/login");
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE);

        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        assertThat("new session id", sessionId, not(equalTo(fixatedSession)));
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}
