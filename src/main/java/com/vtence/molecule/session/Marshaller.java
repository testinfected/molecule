package com.vtence.molecule.session;

public interface Marshaller<T> {
    byte[] marshall(T content) throws Exception;

    T unmarshall(byte[] data) throws Exception;
}
