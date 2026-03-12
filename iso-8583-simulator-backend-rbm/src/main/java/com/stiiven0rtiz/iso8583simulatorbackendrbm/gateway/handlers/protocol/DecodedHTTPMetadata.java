package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

public record DecodedHTTPMetadata(
        int tpduLength,
        int headerLength,
        int bodyLength,
        int totalLength,
        boolean chunked,
        int contentLength,
        int[] crlfPositions,
        byte[] rawMessage
) implements ProtocolMetadata {
}