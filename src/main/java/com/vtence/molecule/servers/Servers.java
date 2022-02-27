package com.vtence.molecule.servers;

import com.vtence.molecule.Server;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public final class Servers {

    private static final List<String> SUPPORTED = asList("Undertow", "Simple");

    private static final List<? extends Class<? extends Server>> AVAILABLE = SUPPORTED.stream()
                                                                                      .map(Servers::loadClass)
                                                                                      .filter(Objects::nonNull)
                                                                                      .collect(toList());

    private static class NoneAvailableException extends RuntimeException {
        public String getMessage() {
            return "No server implementation is available. Add Simple or Undertow jars to your classpath.";
        }
    }

    private static class CreationFailed extends RuntimeException {
        private final Class<? extends Server> clazz;

        public CreationFailed(Class<? extends Server> clazz, Throwable cause) {
            super(cause);
            this.clazz = clazz;
        }

        public String getMessage() {
            return "Failed to create " + clazz.getSimpleName();
        }
    }

    private Servers() {}

    public static Server create(String host, int port) {
        return AVAILABLE.stream()
                        .map(server -> instantiate(server, host, port))
                        .findFirst()
                        .orElseThrow(NoneAvailableException::new);
    }

    private static Server instantiate(Class<? extends Server> clazz, String host, int port) {
        try {
            Constructor<? extends Server> constructor = clazz.getDeclaredConstructor(String.class, Integer.TYPE);
            return constructor.newInstance(host, port);
        } catch (Exception e) {
            throw new CreationFailed(clazz, e);
        }
    }

    private static Class<? extends Server> loadClass(String type) {
        try {
            return Class.forName(className(type)).asSubclass(Server.class);
        } catch (ClassNotFoundException | NoClassDefFoundError notAvailable) {
            return null;
        }
    }

    private static String className(String type) {
        return Servers.class.getPackage().getName() + "." + type + "Server";
    }
}
