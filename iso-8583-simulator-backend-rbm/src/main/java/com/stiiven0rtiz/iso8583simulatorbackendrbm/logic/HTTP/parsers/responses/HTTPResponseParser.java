package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.responses;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;

public sealed interface HTTPResponseParser permits DefaultResponseLoader {
    Transaction parseHTTPMessage(Transaction transaction) throws Exception;
}
