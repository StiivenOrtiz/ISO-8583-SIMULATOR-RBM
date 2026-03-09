package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests.HTTPRequestParser;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses.HTTPResponseParser;

public record ConstructedHTTPMetadata(
        HTTPRequestParser httpRequestParser,
        HTTPResponseParser httpResponseParser
) implements ProtocolMetadata {
}

