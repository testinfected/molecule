package com.vtence.molecule;

import com.vtence.molecule.middlewares.NotFound;
import com.vtence.molecule.middlewares.URLMap;

import java.util.function.Consumer;

public class MiddlewareStack {

    private Middleware pipeline = Middleware.identity();
    private URLMap map;
    private Application runner;
    private Consumer<Application> warmup;

    public MiddlewareStack use(Middleware middleware) {
        if (map != null) {
            pipeline = pipeline.andThen(map);
            map = null;
        }
        pipeline = pipeline.andThen(middleware);
        return this;
    }

    public MiddlewareStack mount(String path, Application app) {
        if (map == null) {
            map = new URLMap();
        }
        map.mount(path, app);
        return this;
    }

    public MiddlewareStack warmup(Consumer<Application> warmup) {
        this.warmup = warmup;
        return this;
    }

    public MiddlewareStack run(Application runner) {
        this.runner = runner;
        return this;
    }

    public Application boot() {
        if (map == null && runner == null) {
            throw new IllegalStateException("No app or mount points defined");
        }

        if (map != null) {
            pipeline = pipeline.andThen(map);
        }

        Application app = pipeline.then(runner != null ? runner : new NotFound());
        if (warmup != null) warmup.accept(app);
        return app;
    }
}
