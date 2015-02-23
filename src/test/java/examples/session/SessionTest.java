package examples.session;

import com.vtence.molecule.FailureReporter;
import com.vtence.molecule.WebServer;
import com.vtence.molecule.support.Delorean;
import com.vtence.molecule.support.StackTrace;
import com.vtence.molecule.testing.http.UrlEncodedForm;
import com.vtence.molecule.testing.http.HttpRequest;
import com.vtence.molecule.testing.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.vtence.molecule.testing.http.HttpResponseAssert.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
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
        server.failureReporter(new FailureReporter() {
            public void errorOccurred(Throwable e) {
                error = e;
            }
        });
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
    public void creatingAPersistentSession() throws IOException {
        response = request.content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        assertThat(response).hasCookie(SESSION_COOKIE).hasMaxAge(-1);
    }

    @Test
    public void noteThatPersistentSessionCookiesAreNotRefreshed() throws IOException {
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
    public void creatingASessionWhichExpires() throws IOException {
        sessions.expireAfter(FIVE_MIN);

        response = request.content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
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
    public void trackingASessionAcrossRequests() throws IOException {
        response = request.but().content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();
        String sessionId = response.cookie(SESSION_COOKIE).getValue();

        response = request.but().cookie(SESSION_COOKIE, sessionId).get("/");
        assertNoError();
        assertThat(response).hasBodyText("Hello, Vincent");
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

    @Test public void
    attemptingToUseAnExpiredSession() throws Exception {
        sessions.expireAfter(FIVE_MIN);

        response = request.but().content(new UrlEncodedForm().addField("username", "Vincent")).post("/login");
        assertNoError();

        delorean.travelInTime(SECONDS.toMillis(FIVE_MIN));

        response = request.but().get("/");
        assertNoError();
        assertThat(response).hasNoCookie(SESSION_COOKIE)
                            .hasBodyText("Hello, Guest");
    }

    private void assertNoError() {
        if (error != null) fail(StackTrace.of(error));
    }
}