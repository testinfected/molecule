package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.FlashHash;
import com.vtence.molecule.session.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.vtence.molecule.testing.ResponseAssert.assertThat;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FlashTest {

    Flash flash = new Flash();

    Request request = new Request();
    Response response = new Response();

    Session session = new Session();

    @Rule
    public ExpectedException error = ExpectedException.none();

    @Before
    public void storeEmptyFlash() {
        session.put(FlashHash.class, new HashMap<>());
        session.bind(request);
    }

    @Test
    public void createsFlashHashFromSessionValues() throws Throwable {
        flashes().put("greeting", "Hello");
        flashes().put("farewell", "Goodbye");

        flash.connectTo(this::printFlashContent);
        flash.handle(request, response);

        assertNoError();
        assertThat(response).hasBodyText("{greeting=Hello, farewell=Goodbye}");
    }

    @Test
    public void complainsIfNoSessionFound() throws Throwable {
        Session.unbind(request);

        error.expect(IllegalStateException.class);
        flash.handle(request, response);
    }

    @Test
    public void createsFreshFlashHashWhenNoneExists() throws Throwable {
        session.clear();

        flash.connectTo(this::printFlashContent);
        flash.handle(request, response);

        assertNoError();
        assertThat(response).hasBodyText("{}");
    }

    @Test
    public void storesFlashEntriesInSessionWhenDone() throws Throwable {
        Map<String, String> entries = new HashMap<>();
        entries.put("farewell", "Goodbye");
        flash.connectTo(writeToFlash(entries));

        flash.handle(request, response);

        response.done();
        assertNoError();

        assertThat("fresh flashes count", flashes(), aMapWithSize(1));
        assertThat("fresh flashes", flashes(), hasEntry("farewell", "Goodbye"));
    }

    @Test
    public void doesNotWriteEmptyFlashToSession() throws Throwable {
        session.clear();

        flash.connectTo(writeToFlash(emptyMap()));
        flash.handle(request, response);

        response.done();
        assertNoError();
        assertThat("done flashes", flashes(), nullValue());
    }

    @Test
    public void unbindsFlashFromRequestOnceDone() throws Throwable {
        flash.handle(request, response);
        response.done();

        assertNoError();
        assertThat("flash", FlashHash.get(request), nullValue());
    }

    @Test
    public void unbindsFlashFromRequestWhenExceptionOccurs() throws Throwable {
        flash.connectTo(crashWith(new Exception("Internal error!")));
        error.expectMessage("Internal error!");
        try {
            flash.handle(request, response);
        } finally {
            assertThat("flash", FlashHash.get(request), nullValue());
        }
    }

    @Test
    public void unbindsFlashFromRequestInCaseOfDeferredErrorAsWell() throws Throwable {
        flash.handle(request, response);
        response.done(new Exception("Internal error!"));

        assertThat("flash", FlashHash.get(request), nullValue());
    }

    @Test
    public void complainsIfSessionRemovedDuringProcessing() throws Throwable {
        flash.connectTo((request, response) -> Session.unbind(request));

        error.expect(IllegalStateException.class);
        response.done();

        flash.handle(request, response);

        awaitCompletion();
    }

    private Application crashWith(Exception error) {
        return (request, response) -> {
            throw error;
        };
    }

    private Application writeToFlash(Map<String, String> values) {
        return (request, response) -> {
            FlashHash flash = FlashHash.get(request);
            assertThat("request flash", flash, notNullValue());

            flash.putAll(values);
        };
    }

    private void printFlashContent(Request request, Response response) throws Exception {
        FlashHash flash = FlashHash.get(request);
        assertThat("request flash", flash, notNullValue());
        response.done(flash.toMap().toString());
    }

    private Map<String, String> flashes() {
        return session.get(FlashHash.class);
    }

    private void assertNoError() throws Throwable {
        awaitCompletion();
    }

    private void awaitCompletion() throws Throwable {
        try {
            response.await();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
