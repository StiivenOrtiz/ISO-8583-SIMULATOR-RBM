package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol.ProtocolType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util.HexParser.toHexNoSpace;

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

    public TransactionContext(byte[] rawRequest, ProtocolType protocolType) {
        this.receivedAt = LocalDateTime.now();
        this.rawRequest = toHexNoSpace(rawRequest);
        this.protocolType = protocolType;
    }

    private TransactionContext(ProtocolType protocolType, LocalDateTime receivedAt) {
        this.protocolType = protocolType;
        this.receivedAt = receivedAt;
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

    public TransactionContext copyAndDestroy() {
        TransactionContext copy = new TransactionContext(this.protocolType, this.receivedAt);

        copy.transactionId = this.transactionId;
        this.transactionId = null;
        copy.transaction = this.transaction;
        this.transaction = null;
        copy.error = this.error;
        this.error = null;
        copy.rawRequest = this.rawRequest;
        this.rawRequest = null;
        copy.rawResponse = this.rawResponse;
        this.rawResponse = null;
        copy.constructedAt = this.constructedAt;
        this.constructedAt = null;
        copy.processedAt = this.processedAt;
        this.processedAt = null;
        copy.respondedAt = this.respondedAt;
        this.respondedAt = null;

        return copy;
    }
}