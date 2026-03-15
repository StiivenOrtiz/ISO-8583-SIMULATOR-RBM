package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;

import java.util.HashMap;
import java.util.Map;

public class ParserUtils {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    public static int bcdToInt(byte[] bcd) {
        int result = 0;
        for (byte b : bcd) {
            int high = (b >> 4) & 0xF;
            int low = b & 0xF;
            result = result * 100 + high * 10 + low;
        }
        return result;
    }

    public static String asciiToString(byte[] bytes) {
        return new String(bytes);
    }

    public static  String getMethod(byte[] rawMessage, int headerLength, int tpduLength) {
        String method = new String(rawMessage, tpduLength, headerLength);
        int firstSpace = method.indexOf(' ');
        if (firstSpace == -1) {
            return method; // No space found, return the whole string
        }
        return method.substring(0, firstSpace);
    }

    public static String getPathWithoutQuery(byte[] rawMessage, int headerLength, int tpduLength) {
        String method = new String(rawMessage, tpduLength, headerLength);
        int firstSpace = method.indexOf(' ');
        if (firstSpace == -1) {
            return ""; // No space found, return empty path
        }
        String pathWithQuery = method.substring(firstSpace + 1);
        int queryStart = pathWithQuery.indexOf('?');
        if (queryStart == -1) {
            return pathWithQuery; // No query string, return the whole path
        }
        return pathWithQuery.substring(0, queryStart);
    }

    public static Map<String, byte[]> extractQueryParams(DecodedHTTPMetadata metadata) {
        byte[] raw = metadata.rawMessage();
        int start = metadata.tpduLength();
        int end = raw.length;

        Map<String, byte[]> params = new HashMap<>();

        int queryStart = -1;
        int queryEnd = -1;

        for (int i = start; i < end; i++)
            if (raw[i] == '?') {
                queryStart = i + 1;
                break;
            }

        if (queryStart == -1)
            return params;

        for (int i = queryStart; i < end; i++)
            if (raw[i] == ' ') {
                queryEnd = i;
                break;
            }

        if (queryEnd == -1)
            queryEnd = end;

        int keyStart = queryStart;
        int valueStart = -1;

        for (int i = queryStart; i <= queryEnd; i++) {
            boolean endParam = (i == queryEnd || raw[i] == '&');

            if (raw[i] == '=' && valueStart == -1) {
                valueStart = i + 1;
                continue;
            }

            if (endParam) {
                if (valueStart != -1) {
                    int keyLength = (valueStart - keyStart - 1);
                    byte[] keyBytes = new byte[keyLength];
                    System.arraycopy(raw, keyStart, keyBytes, 0, keyLength);

                    int valueLength = i - valueStart;
                    byte[] valueBytes = new byte[valueLength];
                    System.arraycopy(raw, valueStart, valueBytes, 0, valueLength);

                    params.put(new String(keyBytes), valueBytes);
                }

                keyStart = i + 1;
                valueStart = -1;
            }
        }

        return params;
    }
}