package com.vtence.molecule.lib;

public final class Hex {

    private static final char[] alphabet = "0123456789abcdef".toCharArray();

    public static String from(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = alphabet[v >>> 4];
            hex[i * 2 + 1] = alphabet[v & 0x0F];
        }
        return new String(hex);
    }

    Hex() {}
}
