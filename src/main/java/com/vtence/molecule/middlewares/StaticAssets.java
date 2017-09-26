package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StaticAssets implements Middleware {

    private final Application fileServer;
    private final List<String> urls = new ArrayList<>();

    private String indexFile = "index.html";

    public StaticAssets(Application fileServer, String... urls) {
        this.fileServer = fileServer;
        serve(urls);
    }

    public StaticAssets serve(String... urls) {
        this.urls.addAll(Arrays.asList(urls));
        return this;
    }

    public StaticAssets index(String indexFile) {
        this.indexFile = indexFile;
        return this;
    }

    public Application then(Application next) {
        return Application.of(request -> canServe(request.path()) ? serve(request) :  next.handle(request));
    }

    private Response serve(Request request) throws Exception {
        if (targetsDirectory(request)) {
            request.path(request.path() + indexFile);
        }
        return fileServer.handle(request);
    }

    private boolean targetsDirectory(Request request) {
        return request.path().endsWith("/");
    }

    private boolean canServe(String path) throws Exception {
        return routeDefinedFor(path).isPresent();
    }

    private Optional<String> routeDefinedFor(String path) {
        return urls.stream().filter(path::startsWith).findFirst();
    }
}