package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.util;

import io.netty.util.AttributeKey;

@Deprecated
public final class TransactionAttributes {
    private TransactionAttributes() {}

    public static final AttributeKey<io.netty.util.concurrent.Promise<Long>> ATTR_TX_ID =
            AttributeKey.valueOf("transactionId");
}
