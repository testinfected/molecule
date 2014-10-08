package com.vtence.molecule.http;

// todo group into request and response headers
// todo provide a more complete list with descriptions (see wikipedia)

// todo consistently use across codebase
public final class HeaderNames {
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ALLOW = "Allow";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DATE = "Date";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
    public static final String SERVER = "Server";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    private HeaderNames() {}
}
