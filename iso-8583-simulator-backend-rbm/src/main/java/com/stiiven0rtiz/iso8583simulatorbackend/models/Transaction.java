package com.stiiven0rtiz.iso8583simulatorbackend.models;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction.java
 * <p>
 * This class represents a Transaction entity in the database.
 *
 * @version 5.0
 */
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @SequenceGenerator(
            name = "tx_seq",
            sequenceName = "tx_seq"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "tx_seq"
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid" , unique = true, nullable = false)
    private String uuid;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "tx_timestamp")
    private LocalDateTime txTimestamp;

    @Column(name = "terminal")
    private String terminal;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "franchise")
    private String franchise;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "mti")
    private String mti;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "rrn", length = 12)
    private String rrn;

    @Column(name = "auth_code", length = 6)
    private String authCode;

    @Column(name = "bitmap_primary", length = 16)
    private String bitmapPrimary;

    @Column(name = "bitmap_secondary", length = 16)
    private String bitmapSecondary;

    @Lob
    @Column(name = "hex_request")
    private String hexRequest;

    @Lob
    @Column(name = "hex_response")
    private String hexResponse;

    @Column(name = "artificial_delay")
    private Integer artificialDelay;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "constructed_at")
    private LocalDateTime constructedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "response_sent_at")
    private LocalDateTime responseSentAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Iso8583Field> iso8583Fields = new ArrayList<>();

    public void addField(Iso8583Field field) {
        iso8583Fields.add(field);
        field.setTransaction(this);
    }
}