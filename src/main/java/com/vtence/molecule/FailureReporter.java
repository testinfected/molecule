package com.vtence.molecule;

public interface FailureReporter {

    FailureReporter IGNORE = error -> {};

    void errorOccurred(Throwable error);
}