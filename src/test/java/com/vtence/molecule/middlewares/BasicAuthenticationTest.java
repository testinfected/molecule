package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.Authenticator;
import com.vtence.molecule.lib.MimeEncoder;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.ACCEPTED;
import static com.vtence.molecule.http.HttpStatus.BAD_REQUEST;
import static com.vtence.molecule.http.HttpStatus.UNAUTHORIZED;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class BasicAuthenticationTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    Authenticator authenticator = context.mock(Authenticator.class);

    BasicAuthentication authentication = new BasicAuthentication("WallyWorld", authenticator);
    MimeEncoder mime = MimeEncoder.inUtf8();

    @Test
    public void
    issuesAChallengeWhenNoCredentialsAreSpecified() throws Exception {
        Response response = authentication.then(request -> Response.ok())
                                          .handle(Request.get("/"));

        assertUnauthorized(response);
    }

    @Test
    public void
    rejectsUnsupportedAuthenticationSchemes() throws Exception {
        Response response = authentication.then(request -> Response.ok())
                                          .handle(Request.get("/")
                                                         .header("Authorization", "Unsupported Scheme"));

        assertThat(response)
                .hasStatus(BAD_REQUEST)
                .isDone();
    }

    @Test
    public void authorizesValidCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(authenticator).authenticate("joe", "secret");
                will(returnValue(of("joe")));
        }});

        Response response = authentication.then(echoRemoteUser())
                                          .handle(Request.get("/")
                                                         .header("Authorization", basic("joe:secret")));

        assertThat(response)
                .hasBodyText("user: joe")
                .hasStatus(ACCEPTED);
    }

    @Test
    public void rejectsInvalidCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(authenticator).authenticate("joe", "bad secret");
                will(returnValue(empty()));
        }});

        Response response = authentication.then(request -> Response.ok())
                                          .handle(Request.get("/")
                                                         .header("Authorization", basic("joe:bad secret")));

        assertUnauthorized(response);
    }

    private void assertUnauthorized(Response response) {
        assertThat(response).hasStatus(UNAUTHORIZED)
                            .hasHeader("WWW-Authenticate", "Basic realm=\"WallyWorld\"")
                            .hasContentType("text/plain")
                            .isEmpty()
                            .isDone();
    }

    private Application echoRemoteUser() {
        return Application.of(request -> {
            String user = request.attribute("REMOTE_USER");
            return Response.of(ACCEPTED)
                           .done("user: " + user);
        });
    }

    private String basic(String src) {
        return "Basic " + mime.encode(src);
    }
}