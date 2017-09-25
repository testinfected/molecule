package com.vtence.molecule.routing;

import com.vtence.molecule.Application;
import com.vtence.molecule.Request;
import com.vtence.molecule.lib.matchers.Matcher;

public interface Route extends Matcher<Request>, Application {
}