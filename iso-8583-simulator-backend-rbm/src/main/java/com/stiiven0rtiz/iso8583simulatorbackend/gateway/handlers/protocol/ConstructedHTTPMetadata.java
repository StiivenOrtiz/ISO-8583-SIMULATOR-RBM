package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.HTTPDefinition;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public record ConstructedHTTPMetadata(
        HTTPDefinition httpDefinition,
        Transaction drawTransaction,
        String TPDU
) implements ProtocolMetadata {
}

