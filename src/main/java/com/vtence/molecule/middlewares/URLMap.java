package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Middleware;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class URLMap implements Middleware {

    private final List<Mount> mounts = new ArrayList<>();

    public URLMap mount(String path, Application app) {
        mounts.add(new Mount(path, app));
        sortByMostSpecificPaths(mounts);
        return this;
    }

    public Application then(Application next) {
        return request -> mountFor(request).orElse(new Mount(next)).handle(request);
    }

    private void sortByMostSpecificPaths(List<Mount> mounts) {
        mounts.sort((mount1, mount2) -> mount2.mountPoint.length() - mount1.mountPoint.length());
    }

    private Optional<Mount> mountFor(Request request) {
        return mounts.stream()
                     .filter(m -> m.matches(request))
                     .findFirst();
    }

    public interface MountPoint {
        String app();

        String uri(String path);

        static MountPoint get(Request request) {
            MountPoint mount = request.attribute(MountPoint.class);
            if (mount == null) throw new IllegalArgumentException("Mount point not found");
            return mount;
        }
    }

    private static class Mount implements Application, MountPoint {

        private final String mountPoint;
        private final Application app;

        public Mount(Application app) {
            this("/", app);
        }

        public Mount(String mountPoint, Application app) {
            this.mountPoint = mountPoint;
            this.app = app;
        }

        public String app() {
            return mountPoint;
        }

        public boolean matches(Request request) {
            return request.path().startsWith(mountPoint) && pathInfo(request).startsWith("/");
        }

        public String pathInfo(Request request) {
            if (mountPoint.equals("/")) return request.path();
            String pathInfo = request.path().replaceFirst(mountPoint, "");
            return pathInfo.isEmpty() ? "/" : pathInfo;
        }

        public Response handle(Request request) throws Exception {
            request.path(pathInfo(request));
            request.attribute(MountPoint.class, this);
            return app.handle(request);
        }

        public String uri(String path) {
            if (mountPoint.equals("/")) return path;
            return mountPoint.concat(path.endsWith("/") ? stripTrailingSlash(path) : path);
        }

        private String stripTrailingSlash(String path) {
            return path.replaceAll("/$", "");
        }
    }
}