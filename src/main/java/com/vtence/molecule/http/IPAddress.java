package com.vtence.molecule.http;

public class IPAddress {

    public static String format(String address) {
        return isIPv6(address) ? toIPv6Reference(address) : address;
    }

    public static boolean isIPv6(String address) {
        return address.contains(":");
    }

    public static String toIPv6Reference(String ipv6Address) {
        return "[" + ipv6Address + "]";
    }

    private IPAddress() {}
}
