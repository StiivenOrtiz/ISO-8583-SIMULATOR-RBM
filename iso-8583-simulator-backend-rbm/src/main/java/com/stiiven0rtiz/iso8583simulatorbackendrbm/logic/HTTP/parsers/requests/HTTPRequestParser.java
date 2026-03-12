package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;

public sealed interface HTTPRequestParser permits DefaultRequest, RequestDigitalVoucherParser {
    Transaction parseHTTPMessage(byte[] rawMessagem, DecodedHTTPMetadata decodedHTTPMetadata) throws Exception;
}
