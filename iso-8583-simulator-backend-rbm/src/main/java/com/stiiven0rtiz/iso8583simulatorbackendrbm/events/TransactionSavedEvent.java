package com.stiiven0rtiz.iso8583simulatorbackendrbm.events;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;

public record TransactionSavedEvent(Transaction transaction) {
}
