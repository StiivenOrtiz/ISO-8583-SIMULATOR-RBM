package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util;

public class Iso8583ProtocolDetector {
    public static boolean isIso8583(byte[] bcdBytes) {
        if (bcdBytes == null || bcdBytes.length != 2)
            return false;

        int d1 = (bcdBytes[0] >> 4) & 0x0F; // version
        int d2 = bcdBytes[0] & 0x0F;        // message class
        int d3 = (bcdBytes[1] >> 4) & 0x0F; // function
        int d4 = bcdBytes[1] & 0x0F;        // origin (no se usa aquí)

        // validate that all digits are between 0 and 9
        if (d1 > 9 || d2 > 9 || d3 > 9 || d4 > 9)
            return false;

        boolean versionValid = (d1 == 0 || d1 == 1 || d1 == 2 || d1 == 9);
        boolean classValid = d2 >= 1;
        boolean functionValid = d3 <= 4;

        return versionValid && classValid && functionValid;
    }
}
