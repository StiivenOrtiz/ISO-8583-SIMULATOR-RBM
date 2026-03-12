package com.stiiven0rtiz.iso8583simulatorbackendrbm.dto;

import java.util.List;

public record TransactionsStatistics(
        long totalTransactions,
        List<BucketSeries> series
) {
}
