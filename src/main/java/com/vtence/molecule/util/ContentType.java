package com.vtence.molecule.util;

import com.vtence.molecule.Response;

import java.nio.charset.Charset;

import static com.vtence.molecule.HttpHeaders.CONTENT_TYPE;

public class ContentType {

    private final String type;
    private final String subType;
    private final String charset;

    public static ContentType of(Response response) {
        String header = response.get(CONTENT_TYPE);
        return header != null ? parse(header) : null;
    }

    public static ContentType parse(String header) {
        return from(new Header(header));
    }

    public static ContentType from(Header header) {
        Header.Value contentType = header.first();
        String[] tokens = contentType.value().split("/");
        return new ContentType(type(tokens), subType(tokens), charset(contentType));
    }

    private static String type(String[] tokens) {
        return tokens[0];
    }

    private static String subType(String[] tokens) {
        return tokens.length > 1 ? tokens[1] : null;
    }

    private static String charset(Header.Value header) {
        return header.parameter("charset");
    }

    public ContentType(String type, String subType, String charset) {
        this.type = type;
        this.subType = subType;
        this.charset = charset;
    }

    public String mediaType() {
        return type + "/" + subType;
    }

    public String type() {
        return type;
    }

    public String subType() {
        return subType;
    }

    public Charset charset() {
        return charset != null ? Charset.forName(charset) : null;
    }

    public String charsetName() {
        return charset;
    }

    public String toString() {
        return mediaType() + (charset != null ? "; charset=" + charset : "");
    }
}
