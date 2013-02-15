package com.vtence.molecule.decoration;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface Layout {

    void render(Writer out, Map<String, Object> fragments) throws IOException;
}
