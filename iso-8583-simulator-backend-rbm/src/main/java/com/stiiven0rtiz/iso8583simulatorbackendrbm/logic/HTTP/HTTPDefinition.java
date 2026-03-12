package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.HTTPRequestParser;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses.HTTPResponseParser;

public record HTTPDefinition(
        HTTPRequestParser httpRequestParser,
        HTTPResponseParser httpResponseParser
) {
}
