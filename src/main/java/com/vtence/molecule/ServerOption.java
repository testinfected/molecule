package com.vtence.molecule;

/**
 * Server options, which might or might not be supported by the underlying server.
 */
public enum ServerOption {
    /**
     * HTTP/2 support
     */
    HTTP_2,
    /**
     * Server internal logging
     */
    LOGGING,
}
