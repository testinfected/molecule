package com.vtence.molecule;

@FunctionalInterface
public interface Application {

    Response handle(Request request) throws Exception;
}