package com.vtence.molecule.templating;

import com.vtence.molecule.Response;

import java.io.IOException;

public interface Template {

    void render(Response response, Object context) throws IOException;
}
