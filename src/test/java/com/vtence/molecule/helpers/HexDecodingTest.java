package com.vtence.molecule.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HexDecodingTest {

    @Parameter(0)
    public String hex;
    @Parameter(1)
    public byte[] bytes;

    HexEncoder codec = new HexEncoder();

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return asList(new Object[][]{
                {"", new byte[]{}},
                {"65a30270cf4b", new byte[]{0x65, (byte) 0xa3, 0x02, 0x70, (byte) 0xcf, 0x4b},},
                {"ffea00749c2b542948c7d3aa", new byte[]{(byte) 0xff, (byte) 0xea, 0x00, 0x74, (byte) 0x9c, 0x2b,
                        0x54, 0x29, 0x48, (byte) 0xc7, (byte) 0xd3, (byte) 0xaa},},
        });
    }

    @Test
    public void
    convertsHexRepresentationToBytes() {
        assertArrayEquals("bytes content", codec.fromHex(hex), bytes);
    }
}