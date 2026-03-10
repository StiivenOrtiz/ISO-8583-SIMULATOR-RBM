package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP;

import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests.HTTPRequestParser;
import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses.HTTPResponseParser;

public record HTTPDefinition(
        HTTPRequestParser httpRequestParser,
        HTTPResponseParser httpResponseParser
) {
}
