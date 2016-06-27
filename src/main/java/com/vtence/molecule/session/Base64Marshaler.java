package com.vtence.molecule.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64Marshaler implements SessionCookieEncoder {

    private static final byte[] NO_LINE_BREAK = new byte[0];

    private final Base64.Encoder encoder = Base64.getMimeEncoder(0, NO_LINE_BREAK);
    private final Base64.Decoder decoder = Base64.getMimeDecoder();

    public String encode(Session data) {
        ByteArrayOutputStream buffer;
        try {
            buffer = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(buffer));
            output.writeObject(data);
            output.flush();
            output.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return encoder.encodeToString(buffer.toByteArray());
    }

    public Session decode(String encoded) {
        Object data;
        try {
            ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(
                    new ByteArrayInputStream(decoder.decode(encoded))));
            data = input.readObject();
            return (Session) data;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}