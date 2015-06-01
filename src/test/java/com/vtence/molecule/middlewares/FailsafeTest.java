package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import org.junit.Test;

import static com.vtence.molecule.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;

public class FailsafeTest {
    Failsafe failsafe = new Failsafe();

    String errorMessage = "An error occurred!";
    Error error = newError(errorMessage);

    Request request = new Request();
    Response response = new Response();

    @Test public void
    setsStatusToInternalServerError() throws Exception {
        respondWithError(failsafe, error);

        assertThat(response).hasStatus(INTERNAL_SERVER_ERROR);
    }

    @Test public void
    rendersErrorStackTrace() throws Exception {
        respondWithError(failsafe, error);

        assertThat(response).hasBodyText(containsString(errorMessage))
                .hasBodyText(containsString("stack.trace(line:1)"))
                .hasBodyText(containsString("stack.trace(line:2)"))
                .hasBodyText(not(containsString("Caused by:")));
    }

    @Test public void
    rendersChainOfErrorCauses() throws Exception {
        Error rootCause = newError("root cause of error", null, stackFrame("root.cause.stack", 1));
        Error cause = newError("cause of error", rootCause, stackFrame("cause.of.error.stack", 1));
        Error errorWithCause = newError("this error has a cause", cause);

        respondWithError(failsafe, errorWithCause);

        assertThat(response).hasBodyText(containsString("Caused by: java.lang.Error: cause of error"))
                .hasBodyText(containsString("cause.of.error.stack.trace(line:1)"))
                .hasBodyText(containsString("Caused by: java.lang.Error: root cause"))
                .hasBodyText(containsString("root.cause.stack.trace(line:1)"));
    }

    @Test public void
    respondsWithHtmlContentUtf8Encoded() throws Exception {
        respondWithError(failsafe, error);

        assertThat(response).hasContentType("text/html; charset=utf-8");
    }

    private void
    respondWithError(final Failsafe failsafe, final Error error) throws Exception {
        failsafe.connectTo(new Application() {
            public void handle(Request request, Response response) throws Exception {
                throw error;
            }
        });
        failsafe.handle(request, response);
    }

    private Error newError(String errorMessage) {
        return newError(errorMessage, null);
    }

    private Error newError(String errorMessage, Error cause) {
        return newError(errorMessage, cause, stackFrame("error.stack", 1), stackFrame("error.stack", 2));
    }

    private Error newError(String errorMessage, Error cause, StackTraceElement... stackTrace) {
        Error error = new Error(errorMessage, cause);
        error.setStackTrace(stackTrace);
        return error;
    }

    private StackTraceElement stackFrame(String className, int lineNumber) {
        return new StackTraceElement(className, "trace", "line", lineNumber);
    }
}