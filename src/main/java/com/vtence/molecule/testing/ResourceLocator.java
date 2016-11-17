package com.vtence.molecule.testing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ResourceLocator {

    public static File locateOnClasspath(String resource) {
        return onClasspath().locate(resource);
    }

    public static ResourceLocator onClasspath() {
        return new ResourceLocator(Thread.currentThread().getContextClassLoader());
    }

    private final ClassLoader classLoader;

    public ResourceLocator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public File locate(String resource) {
        URL location = classLoader.getResource(resource);
        if (location == null) throw new IllegalArgumentException("Cannot find resource " + resource);
        try {
            return new File(location.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(resource, e);
        }
    }
}