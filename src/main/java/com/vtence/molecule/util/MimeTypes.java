package com.vtence.molecule.util;

import java.util.HashMap;
import java.util.Map;

public final class MimeTypes {

    public static final String HTML = "text/html";
    public static final String TEXT = "text/plain";
    public static final String CSS = "text/css";
    public static final String JAVASCRIPT = "application/javascript";
    public static final String JSON = "application/json";
    public static final String PNG = "image/png";
    public static final String GIF = "image/gif";
    public static final String JPEG = "image/jpeg";
    public static final String ICON = "image/x-icon";
    private static final String BINARY_DATA = "application/octet-stream";

    private final Map<String, String> knownTypes = new HashMap<String, String>();

    public static MimeTypes defaults() {
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.map("txt", TEXT);
        mimeTypes.map("html", HTML);
        mimeTypes.map("css", CSS);
        mimeTypes.map("js", JAVASCRIPT);
        mimeTypes.map("png", PNG);
        mimeTypes.map("gif", GIF);
        mimeTypes.map("jpg", JPEG);
        mimeTypes.map("jpeg", JPEG);
        mimeTypes.map("ico", ICON);
        return mimeTypes;
    }

    public void map(String extension, String mimeType) {
        knownTypes.put(extension, mimeType);
    }

    public String guessFrom(String filename) {
        for (String ext : knownTypes.keySet()) {
            if (filename.endsWith("." + ext)) return knownTypes.get(ext);
        }
        return BINARY_DATA;
    }
}
