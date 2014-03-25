package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;
import com.vtence.molecule.util.RequestWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.vtence.molecule.middlewares.StaticAssets.RequestOverride.override;

public class StaticAssets extends AbstractMiddleware {

    private final Application fileServer;
    private final List<String> urls = new ArrayList<String>();
    private String indexFile;

    public StaticAssets(Application fileServer, String... urls) {
        this.fileServer = fileServer;
        this.urls.addAll(Arrays.asList(urls));
    }

    public StaticAssets serve(String... urls) {
        this.urls.addAll(Arrays.asList(urls));
        return this;
    }

    public StaticAssets index(String indexFile) {
        this.indexFile = indexFile;
        return this;
    }

    public void handle(Request request, Response response) throws Exception {
        if (canServe(request.pathInfo())) {
            serve(request, response);
        } else {
            forward(request, response);
        }
    }

    private void serve(Request request, Response response) throws Exception {
        if (targetsDirectory(request)) {
            request = override(request).withPath(request.pathInfo() + indexFile);
        }
        fileServer.handle(request, response);
    }

    private boolean targetsDirectory(Request request) {
        return request.pathInfo().endsWith("/");
    }

    private boolean canServe(String path) throws Exception {
        return routeDefinedFor(path);
    }

    private boolean routeDefinedFor(String path) {
        for (String url : urls) {
            if (path.startsWith(url)) return true;
        }
        return false;
    }

    public static class RequestOverride extends RequestWrapper {
        private String path;

        public static RequestOverride override(Request request) {
            return new RequestOverride(request);
        }

        public RequestOverride(Request request) {
            super(request);
        }

        public RequestOverride withPath(String path) {
            this.path = path;
            return this;
        }

        public String pathInfo() {
            return path != null ? path : super.pathInfo();
        }
    }
}
