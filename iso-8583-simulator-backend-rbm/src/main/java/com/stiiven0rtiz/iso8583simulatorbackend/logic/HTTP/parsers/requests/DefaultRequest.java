package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.requests;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.DecodedHTTPMetadata;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;
import org.springframework.stereotype.Component;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.HexParser.toHexNoSpace;

@Component
@HTTPRequestParserType(HTTPRequestsParsers.NOT_MAPPED)
public non-sealed class DefaultRequest implements HTTPRequestParser {
    @Override
    public Transaction parseHTTPMessage(byte[] message, DecodedHTTPMetadata decodedHTTPMetadata) {
        Transaction tx = new Transaction();

        tx.setHexRequest(toHexNoSpace(message));

        return tx;
    }
}
