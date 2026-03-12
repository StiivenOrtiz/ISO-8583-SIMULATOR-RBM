package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.stereotype.Component;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.HexParser.toHexNoSpace;

@Component
@HTTPRequestParserType(HTTPRequestsParsers.NOT_MAPPED)
public non-sealed class DefaultRequest implements HTTPRequestParser {
    @Override
    public Transaction parseHTTPMessage(byte[] rawMessagem, DecodedHTTPMetadata decodedHTTPMetadata) {
        Transaction tx = new Transaction();

        tx.setHexRequest(toHexNoSpace(rawMessagem));

        return tx;
    }
}
