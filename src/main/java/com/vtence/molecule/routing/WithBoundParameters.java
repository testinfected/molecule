package com.vtence.molecule.routing;

import com.vtence.molecule.Request;

import java.util.Map;

public interface WithBoundParameters {

    Map<String, String> parametersBoundTo(String path);

    default void addParametersTo(Request request) {
        Map<String, String> dynamicParameters = parametersBoundTo(request.path());
        for (String name : dynamicParameters.keySet()) {
            request.addParameter(name, dynamicParameters.get(name));
        }
    }
}