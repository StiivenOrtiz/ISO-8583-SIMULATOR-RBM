package com.stiiven0rtiz.iso8583simulatorbackend.services;

import com.stiiven0rtiz.iso8583simulatorbackend.models.Iso8583Field;
import com.stiiven0rtiz.iso8583simulatorbackend.models.IsoMessageType;
import com.stiiven0rtiz.iso8583simulatorbackend.models.Transaction;
import com.stiiven0rtiz.iso8583simulatorbackend.repositories.Iso8583FieldRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Iso8583FieldService.java
 * <p>
 * This service class provides methods to manage Iso8583Field entities,
 * including saving fields and retrieving fields by transaction.
 *
 * @version 1.1
 */
@Service
public class Iso8583FieldService {

    private final Iso8583FieldRepository repository;

    public Iso8583FieldService(Iso8583FieldRepository repository) {
        this.repository = repository;
    }

    public void saveField(Iso8583Field field) {
        repository.save(field);
    }

    /**
     * Retrieve all ISO 8583 fields associated with a specific transaction.
     *
     * @param tx The transaction for which to retrieve fields.
     * @return A list of Iso8583Field entities associated with the transaction.
     */
    public List<Iso8583Field> getFieldsByTransaction(Transaction tx) {
        return repository.findByTransaction(tx);
    }

    public List<Iso8583Field> getFieldsByTransactionAndType(
            Transaction tx,
            IsoMessageType type
    ) {
        return repository.findByTransactionAndMessageType(tx, type);
    }
}
