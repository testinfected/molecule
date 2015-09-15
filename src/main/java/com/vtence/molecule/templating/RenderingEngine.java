package com.vtence.molecule.templating;

import java.io.IOException;
import java.io.Writer;

public interface RenderingEngine {

    void render(Writer out, String templateName, Object context) throws IOException;
}