package com.stiiven0rtiz.iso8583simulatorbackendrbm.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;

public record ResponseMSGHTTP(
        Transaction transaction
) implements ResponseMSG {
}
