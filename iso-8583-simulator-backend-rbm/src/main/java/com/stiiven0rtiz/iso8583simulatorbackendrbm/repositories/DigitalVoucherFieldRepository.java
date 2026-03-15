package com.stiiven0rtiz.iso8583simulatorbackendrbm.repositories;

import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.DigitalVoucherField;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.MessageType;
import com.stiiven0rtiz.iso8583simulatorbackendrbm.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitalVoucherFieldRepository extends JpaRepository<DigitalVoucherField, Long> {

    List<DigitalVoucherField> findByTransaction(Transaction transaction);

    List<DigitalVoucherField> findByTransactionAndMessageType(
            Transaction tx,
            MessageType type
    );

}