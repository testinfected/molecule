package com.vtence.molecule.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer<T> implements Marshaller<T> {

    private final Class<T> type;

    public Serializer(Class<T> type) {
        this.type = type;
    }

    public byte[] marshall(T data) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(buffer);
        output.writeObject(data);
        output.flush();
        output.close();
        return buffer.toByteArray();
    }

    public T unmarshall(byte[] data) throws Exception {
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(data));
        return type.cast(input.readObject());
    }
}
