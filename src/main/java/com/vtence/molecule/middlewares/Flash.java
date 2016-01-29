package com.vtence.molecule.middlewares;

import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.lib.FlashHash;
import com.vtence.molecule.session.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Flash extends AbstractMiddleware {

    public void handle(Request request, Response response) throws Exception {
        FlashHash flash = loadFlash(request);
        flash.bind(request);

        try {
            forward(request, response).whenSuccessful(commitFlash(request)).whenComplete(unbindFlashFrom(request));
        } catch (Throwable error) {
            FlashHash.unbind(request);
            throw error;
        }
    }

    private BiConsumer<Response, Throwable> unbindFlashFrom(final Request request) {
        return (response, throwable) -> FlashHash.unbind(request);
    }

    private FlashHash loadFlash(Request request) {
        Session session = fetchSession(request);
        Map<String, Object> flashes = session.contains(FlashHash.class) ?
                session.remove(FlashHash.class) : new HashMap<>();
        return new FlashHash(flashes);
    }

    private Consumer<Response> commitFlash(Request request) {
        return response -> {
            Session session = fetchSession(request);
            FlashHash flash = FlashHash.get(request);
            flash.sweep();
            if (!session.invalid() && !flash.empty()) {
                session.put(FlashHash.class, flash.toMap());
            }
        };
    }

    private Session fetchSession(Request request) {
        Session session = Session.get(request);
        if (session == null) throw new IllegalStateException("No session bound to request");
        return session;
    }
}
