package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public sealed interface HTTPRequestParser permits DefaultRequest {
    Transaction parseHTTPMessage(byte[] rawMessagem, DecodedHTTPMetadata decodedHTTPMetadata) throws Exception;
}
