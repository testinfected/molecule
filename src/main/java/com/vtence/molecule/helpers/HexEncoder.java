package com.vtence.molecule.helpers;

/**
 * Code taken from <a href="https://stackoverflow.com/a/9855338">Stack Overflow</a>. Attribution goes to <a href="http://stackoverflow.com/users/1284661/maybewecouldstealavan">maybeWeCouldStealAVan</a>.
 */
public final class HexEncoder {

    private static final char[] alphabet = "0123456789abcdef".toCharArray();

    public String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = alphabet[v >>> 4];
            hex[i * 2 + 1] = alphabet[v & 0x0F];
        }
        return new String(hex);
    }

    public byte[] fromHex(String hex) {
        return fromHex(hex.toCharArray());
    }

    public byte[] fromHex(char[] hex) {
        final int len = hex.length;

        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters");
        }

        final byte[] out = new byte[len >> 1];

        // 2 characters form the hex value
        for (int i = 0, j = 0; j < len; i++) {
            out[i] = (byte) ((toDigit(hex[j++]) << 4 | toDigit(hex[j++])) & 0xFF);
        }

        return out;
    }

    private static int toDigit(char c) {
        return Character.digit(c, 16);
    }
}
