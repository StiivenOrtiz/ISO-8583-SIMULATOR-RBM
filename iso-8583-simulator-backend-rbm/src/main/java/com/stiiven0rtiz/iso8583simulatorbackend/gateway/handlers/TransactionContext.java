package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.util.HexParser.toHexNoSpace;

@Getter
public class TransactionContext {

    @Setter
    private Promise<Long> transactionId;
    @Setter
    private Promise<Transaction> transaction;

    @Setter
    private Throwable error;
    private final ProtocolType protocolType;

    private String rawRequest;
    private String rawResponse;

    private final LocalDateTime receivedAt;

    private LocalDateTime constructedAt;
    private LocalDateTime processedAt;
    private LocalDateTime respondedAt;

    public TransactionContext() {
        this.receivedAt = LocalDateTime.now();
        this.protocolType = null;
    }

    public TransactionContext(ByteBuf rawRequest) {
        this.receivedAt = LocalDateTime.now();
        this.rawRequest = toHexNoSpace(rawRequest);
        this.protocolType = ProtocolType.UNKNOWN;
    }

    public TransactionContext(ByteBuf rawRequest, ProtocolType protocolType) {
        this.receivedAt = LocalDateTime.now();
        this.rawRequest = toHexNoSpace(rawRequest);
        this.protocolType = protocolType;
    }

    public void setConstructedMessage() {
        this.constructedAt = LocalDateTime.now();
    }

    public void setResponse(String rawResponse) {
        this.rawResponse = rawResponse;
        this.processedAt = LocalDateTime.now();
    }

    public void setResponded() {
        this.respondedAt = LocalDateTime.now();
    }

    public boolean hasError() {
        return error != null;
    }

}
