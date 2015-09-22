package com.vtence.molecule;

import com.vtence.molecule.middlewares.URLMap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class MiddlewareStack implements Application {

    private final Deque<Middleware> stack = new ArrayDeque<>();

    private URLMap map;
    private Application runner;
    private Application pipeline = (request, response) -> boot().handle(request, response);

    public MiddlewareStack() {}

    public MiddlewareStack use(Middleware middleware) {
        if (map != null) {
            stack.add(map);
        }
        stack.add(middleware);
        return this;
    }

    public MiddlewareStack mount(String path, Application app) {
        if (map == null) {
            map = new URLMap();
        }
        map.mount(path, app);
        return this;
    }

    public MiddlewareStack run(Application runner) {
        this.runner = runner;
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        pipeline.handle(request, response);
    }

    public Application boot() {
        if (map == null && runner == null) {
            throw new IllegalStateException("No app or mount points defined");
        }

        if (map != null) {
            runner = runner != null ? map.mount("/", runner) : map;
        }

        pipeline = assemble();
        return pipeline;
    }

    private Application assemble() {
        Application chain = runner;

        for (Iterator<Middleware> middlewares = stack.descendingIterator(); middlewares.hasNext(); ) {
            Middleware previous = middlewares.next();
            previous.connectTo(chain);
            chain = previous;
        }

        return chain;
    }
}
