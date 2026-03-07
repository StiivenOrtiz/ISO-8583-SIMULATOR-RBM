package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.POJO;

import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.TransactionContext;
import com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol.ProtocolFrame;
import io.netty.util.AttributeKey;

public class ChannelAttributes {
    public static final AttributeKey<TransactionContext> TX_CONTEXT = AttributeKey.valueOf("txContext");

    public static final AttributeKey<ProtocolFrame> TX_PROTOCOL_FRAME = AttributeKey.valueOf("txProtocolFrame");
}
