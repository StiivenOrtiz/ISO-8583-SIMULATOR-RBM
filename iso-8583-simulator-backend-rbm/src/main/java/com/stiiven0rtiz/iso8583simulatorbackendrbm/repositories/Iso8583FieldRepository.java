package com.stiiven0rtiz.iso8583simulatorbackendrbm.repositories;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Iso8583Field;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.MessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Iso8583FieldRepository.java
 * <p>
 * This interface extends JpaRepository to provide CRUD operations and custom queries
 * for Iso8583Field entities.
 *
 * @version 1.1
 */
public interface Iso8583FieldRepository extends JpaRepository<Iso8583Field, Long> {

    // Find all fields by transaction
    List<Iso8583Field> findByTransaction(Transaction transaction);

    List<Iso8583Field> findByTransactionAndMessageType(Transaction tx, MessageType type);
}
