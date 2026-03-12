package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.HTTPDefinition;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;

public record ConstructedHTTPMetadata(
        HTTPDefinition httpDefinition,
        Transaction drawTransaction,
        String TPDU
) implements ProtocolMetadata {
}

