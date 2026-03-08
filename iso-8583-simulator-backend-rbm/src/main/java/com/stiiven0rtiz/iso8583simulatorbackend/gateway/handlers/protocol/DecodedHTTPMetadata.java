package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

public record DecodedHTTPMetadata(
        int tpduLength,
        int headerLength,
        int bodyLength,
        int totalLength,
        boolean chunked,
        int contentLength,
        byte[] rawMessage
) implements ProtocolMetadata {
}