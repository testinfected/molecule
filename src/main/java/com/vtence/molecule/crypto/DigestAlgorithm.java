package com.vtence.molecule.crypto;

public interface DigestAlgorithm {

    byte[] compute(String secret, byte[] content) throws Exception;

    boolean verify(String secret, byte[] content, byte[] digest) throws Exception;
}
