package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util;

public class BytesParser {

    public static byte[] parseHexWithoutSpacesString(String hex) {
        String hexS = hex.replace(" ", "");
        int len = hex.length();
        byte[] bytes = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));

        return bytes;
    }
}
