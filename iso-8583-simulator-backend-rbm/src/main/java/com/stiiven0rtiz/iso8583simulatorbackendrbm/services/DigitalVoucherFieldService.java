package com.stiiven0rtiz.iso8583simulatorbackendrbm.services;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.DigitalVoucherField;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.MessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.repositories.DigitalVoucherFieldRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DigitalVoucherFieldService {

    private final DigitalVoucherFieldRepository repository;

    public DigitalVoucherFieldService(DigitalVoucherFieldRepository repository) {
        this.repository = repository;
    }

    public void saveField(DigitalVoucherField field) {
        repository.save(field);
    }

    public List<DigitalVoucherField> getFieldsByTransaction(Transaction tx) {
        return repository.findByTransaction(tx);
    }

    public List<DigitalVoucherField> getFieldsByTransactionAndType(
            Transaction tx,
            MessageType type
    ) {
        return repository.findByTransactionAndMessageType(tx, type);
    }
}