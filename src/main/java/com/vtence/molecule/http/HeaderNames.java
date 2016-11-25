package com.vtence.molecule.http;

/**
 * HTTP Request and Response Headers (see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>).
 *
 * Not meant to be implemented, but used as static imports.
 */
public interface HeaderNames {
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_ENCODING = "Content-Encoding";
    String CONTENT_LANGUAGE = "Content-Language";
    String CONTENT_LOCATION = "Content-Location";

    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_RANGE = "Content-Range";
    String TRAILER = "Trailer";
    String TRANSFER_ENCODING = "Transfer-Encoding";

    String CACHE_CONTROL = "Cache-Control";
    String EXPECT = "Expect";
    String HOST = "Host";
    String MAX_FORWARDS = "Max-Forwards";
    String PRAGMA = "Pragma";
    String RANGE = "RANGE";
    String TE = "TE";

    String IF_MATCH = "If-Match";
    String IF_NONE_MATCH = "If-None-Match";
    String IF_MODIFIED_SINCE = "If-Modified-Since";
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    String IF_RANGE = "If-Range";

    String ACCEPT = "Accept";
    String ACCEPT_CHARSET = "Accept-Charset";
    String ACCEPT_ENCODING = "Accept-Encoding";
    String ACCEPT_LANGUAGE = "Accept-Language";

    String AUTHORIZATION = "Authorization";
    String PROXY_AUTHORIZATION = "Proxy-Authorization";

    String FROM = "From";
    String REFERER = "Referer";
    String USER_AGENT = "User-Agent";

    String AGE = "Age";
    String EXPIRES = "Expires";
    String DATE = "Date";
    String LOCATION = "Location";
    String RETRY_AFTER = "Retry-After";
    String VARY = "Vary";
    String WARNING = "Warning";

    String ETAG = "ETag";
    String LAST_MODIFIED = "Last-Modified";

    String WWW_AUTHENTICATE = "WWW-Authenticate";
    String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    String ACCEPT_RANGES = "Accept-Ranges";
    String ALLOW = "Allow";
    String SERVER = "Server";

    String SET_COOKIE = "Set-Cookie";
    String COOKIE = "Cookie";

    String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
}