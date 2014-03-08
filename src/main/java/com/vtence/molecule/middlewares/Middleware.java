package com.vtence.molecule.middlewares;

import com.vtence.molecule.Application;

public interface Middleware extends Application {

    void connectTo(Application successor);
}
