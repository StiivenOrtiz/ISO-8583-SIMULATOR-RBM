package com.stiiven0rtiz.iso8583simulatorbackend.websocket.listener;

import com.stiiven0rtiz.iso8583simulatorbackend.events.TransactionCSavedEvent;
import com.stiiven0rtiz.iso8583simulatorbackend.events.TransactionConstructed;
import com.stiiven0rtiz.iso8583simulatorbackend.events.TransactionSavedEvent;
import com.stiiven0rtiz.iso8583simulatorbackend.services.Iso8583FieldService;
import com.stiiven0rtiz.iso8583simulatorbackend.mapper.TransactionMapper;
import com.stiiven0rtiz.iso8583simulatorbackend.websocket.service.TxLiveFeedService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionLiveFeedListener {

    private final TxLiveFeedService liveFeedService;
    private final Iso8583FieldService iso8583FieldService;

    public TransactionLiveFeedListener(TxLiveFeedService liveFeedService, Iso8583FieldService iso8583FieldService) {
        this.liveFeedService = liveFeedService;
        this.iso8583FieldService = iso8583FieldService;
    }

    @EventListener
    public void onTransactionSaved(TransactionSavedEvent event) {
        event.transaction().setIso8583Fields(iso8583FieldService.getFieldsByTransaction(event.transaction()));
        liveFeedService.broadcast(TransactionMapper.toDto(event.transaction()));
    }

    @EventListener
    public void onTransactionConstructed(TransactionConstructed event) {
        liveFeedService.broadcast(TransactionMapper.toDto(event.transaction()));
    }

    @EventListener
    public void onTransactionCSavedEvent(TransactionCSavedEvent event) {
        liveFeedService.broadcast(TransactionMapper.toDto(event.transaction()));
    }
}
