package com.stiiven0rtiz.iso8583simulatorbackendrbm.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "digital_voucher_fields")
public class DigitalVoucherField {

    @Id
    @SequenceGenerator(
            name = "digital_voucher_field_seq",
            sequenceName = "digital_voucher_field_seq"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "digital_voucher_field_seq"
    )
    private Long id;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @Lob
    @Column(name = "field_value")
    private String fieldValue;

    @Column(name = "field_length")
    private int fieldLength;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonBackReference
    private Transaction transaction;
}