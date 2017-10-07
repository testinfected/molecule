package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;

import java.util.Optional;

@FunctionalInterface
public interface Route {

    Optional<Application> route(Request request);
}