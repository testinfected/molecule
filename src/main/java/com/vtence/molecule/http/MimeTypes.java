package com.vtence.molecule.http;

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
    public static final String BINARY_DATA = "application/octet-stream";

    private final Map<String, String> knownTypes = new HashMap<>();

    public static MimeTypes defaults() {
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.register("txt", TEXT);
        mimeTypes.register("html", HTML);
        mimeTypes.register("css", CSS);
        mimeTypes.register("js", JAVASCRIPT);
        mimeTypes.register("png", PNG);
        mimeTypes.register("gif", GIF);
        mimeTypes.register("jpg", JPEG);
        mimeTypes.register("jpeg", JPEG);
        mimeTypes.register("ico", ICON);
        return mimeTypes;
    }

    public static boolean matches(String mediaType, String pattern) {
        return MediaType.parse(pattern).isGeneralizationOf(MediaType.parse(mediaType));
    }

    public void register(String extension, String mimeType) {
        knownTypes.put(extension, mimeType);
    }

    public String guessFrom(String filename) {
        for (String ext : knownTypes.keySet()) {
            if (filename.endsWith("." + ext)) return knownTypes.get(ext);
        }
        return BINARY_DATA;
    }

    private static class MediaType {
        public static final String WILCARD = "*";

        public final String type;
        public final String subtype;

        public MediaType(String type, String subtype) {
            this.type = type;
            this.subtype = subtype;
        }

        public static MediaType parse(String mediaType) {
            String[] parts = mediaType.split("/");
            return parts.length > 1 ? new MediaType(parts[0], parts[1]) : new MediaType(parts[0], WILCARD);
        }

        public boolean isGeneralizationOf(MediaType mime) {
            return (type.equals(mime.type) || type.equals(WILCARD)) &&
                    (subtype.equals(WILCARD) || subtype.equals(mime.subtype));
        }
    }
}
