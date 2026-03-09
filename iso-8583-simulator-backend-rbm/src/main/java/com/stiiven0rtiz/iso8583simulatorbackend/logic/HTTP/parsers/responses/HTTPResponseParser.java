package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses;

import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public sealed interface HTTPResponseParser permits DefaultResponseLoader {
    byte[] parseHTTPMessage(Transaction tx) throws Exception;
}
