package com.vtence.molecule.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64Marshaler implements SessionCookieEncoder {

    private static final byte[] NO_LINE_BREAK = new byte[0];

    private final Base64.Encoder encoder = Base64.getMimeEncoder(0, NO_LINE_BREAK);
    private final Base64.Decoder decoder = Base64.getMimeDecoder();

    public String encode(Session data) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(buffer));
        output.writeObject(data);
        output.flush();
        output.close();
        return encoder.encodeToString(buffer.toByteArray());
    }

    public Session decode(String encoded) throws Exception {
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(
                    new ByteArrayInputStream(decoder.decode(encoded))));
        return (Session) input.readObject();
    }
}