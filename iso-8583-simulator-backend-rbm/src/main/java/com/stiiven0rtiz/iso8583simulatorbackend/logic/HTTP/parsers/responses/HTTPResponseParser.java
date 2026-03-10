package com.stiiven0rtiz.iso8583simulatorbackend.logic.HTTP.parsers.responses;

import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public sealed interface HTTPResponseParser permits DefaultResponseLoader {
    Transaction parseHTTPMessage(Transaction transaction) throws Exception;
}
