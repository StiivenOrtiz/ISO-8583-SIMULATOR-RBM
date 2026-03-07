package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util;

import io.netty.buffer.ByteBuf;

public class HexParser {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    public static String parseHexWithSpaces(String hex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            sb.append(hex, i, Math.min(i + 2, hex.length()));
            if (i + 2 < hex.length()) sb.append(" ");
        }
        return sb.toString();
    }

    public static String parseHexWithoutSpaces(String hex) {
        return hex.replace(" ", "");
    }

    public static String toHexNoSpace(ByteBuf buf) {
        int len = buf.readableBytes();
        StringBuilder sb = new StringBuilder(len * 2);

        for (int i = buf.readerIndex(); i < buf.readerIndex() + len; i++) {
            int v = buf.getUnsignedByte(i);
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
        }

        return sb.toString();
    }
}
