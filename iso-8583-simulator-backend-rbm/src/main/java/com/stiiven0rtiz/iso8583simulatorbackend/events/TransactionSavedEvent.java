package com.stiiven0rtiz.iso8583simulatorbackend.events;

import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;

public record TransactionSavedEvent(Transaction transaction) {
}
