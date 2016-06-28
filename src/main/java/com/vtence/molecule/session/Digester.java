package com.vtence.molecule.session;

public interface Digester {

    String computeDigest(String key, String content) throws Exception;

    boolean checkDigest(String key, String content, String digest) throws Exception;
}
