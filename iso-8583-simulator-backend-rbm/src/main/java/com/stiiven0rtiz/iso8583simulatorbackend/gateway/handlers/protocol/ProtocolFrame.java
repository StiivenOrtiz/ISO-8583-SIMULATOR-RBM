package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.TransactionContext;
import io.netty.buffer.ByteBuf;

public record ProtocolFrame(
        ProtocolType protocol,
        ProtocolMetadata  metadata,
        TransactionContext context
) {
    public ProtocolFrame(
            ProtocolType protocol,
            ProtocolMetadata metadata,
            TransactionContext context
    ) {
        this.protocol = protocol;
        this.metadata = metadata;
        this.context = context;
    }

    public ProtocolFrame(
            ProtocolType protocol,
            ProtocolMetadata metadata,
            ByteBuf rawRequest
    ) {
        this(protocol, metadata, new TransactionContext(rawRequest, protocol));
    }
}
