package com.stiiven0rtiz.iso8583simulatorbackend.gateway.handlers.protocol;

import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public record ResponseMSGHTTP(
        Transaction transaction
) implements ResponseMSG {
}
