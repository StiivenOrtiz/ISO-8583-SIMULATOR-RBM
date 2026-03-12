package com.stiiven0rtiz.iso8583simulatorbackendrbm.dto;

import java.util.List;

public record CurrentInfoTransactions(
        List<String> terminals,
        List<String> franchises,
        List<String> transactionTypes,
        List<String> mtis,
        List<String> statusValues
) {
}
