package com.vtence.molecule;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class MiddlewareStack implements Application {

    private final Deque<Middleware> stack = new ArrayDeque<Middleware>();
    private Application pipeline;

    public MiddlewareStack() {}

    public MiddlewareStack use(Middleware middleware) {
        stack.add(middleware);
        return this;
    }

    public MiddlewareStack run(Application runner) {
        this.pipeline = assemble(runner);
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        if (pipeline == null) throw new IllegalStateException("Nothing to run");
        pipeline.handle(request, response);
    }

    public Application assemble(Application runner) {
        Application chain = runner;

        for (Iterator<Middleware> middlewares = stack.descendingIterator(); middlewares.hasNext(); ) {
            Middleware previous = middlewares.next();
            previous.connectTo(chain);
            chain = previous;
        }
        return chain;
    }
}
