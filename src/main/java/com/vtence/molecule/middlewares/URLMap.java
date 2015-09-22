package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class URLMap implements Application {

    private final List<Mount> mounts = new ArrayList<>();
    private final Application fallback;

    public URLMap() {
        this(new NotFound());
    }

    public URLMap(Application fallback) {
        this.fallback = fallback;
    }

    public URLMap mount(String path, Application app) {
        mounts.add(new Mount(path, app));
        sortByMostSpecificPaths(mounts);
        return this;
    }

    private void sortByMostSpecificPaths(List<Mount> mounts) {
        mounts.sort((mount1, mount2) -> mount2.mountPoint.length() - mount1.mountPoint.length());
    }

    public void handle(Request request, Response response) throws Exception {
        Optional<Mount> mount = mounts.stream().filter(m -> m.matches(request)).findFirst();

        if (mount.isPresent()) {
            mount.get().handle(request, response);
        } else {
            fallback.handle(request, response);
        }
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
            String pathInfo = request.path().replaceFirst(mountPoint, "");
            return pathInfo.isEmpty() ? "/" : pathInfo;
        }

        public String uri(String path) {
            return mountPoint.concat(path.endsWith("/") ? stripTrailingSlash(path) : path);
        }

        private String stripTrailingSlash(String path) {
            return path.replaceAll("/$", "");
        }

        public void handle(Request request, Response response) throws Exception {
            request.path(pathInfo(request));
            request.attribute(MountPoint.class, this);
            app.handle(request, response);
        }
    }
}