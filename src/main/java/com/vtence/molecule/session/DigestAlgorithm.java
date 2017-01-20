package com.vtence.molecule.session;

public interface DigestAlgorithm {

    byte[] compute(String secret, byte[] content) throws Exception;

    boolean verify(String secret, byte[] content, byte[] digest) throws Exception;
}
