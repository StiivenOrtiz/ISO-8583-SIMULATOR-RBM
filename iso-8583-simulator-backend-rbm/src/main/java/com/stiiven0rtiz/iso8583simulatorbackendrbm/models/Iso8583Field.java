package com.stiiven0rtiz.iso8583simulatorbackendrbm.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Iso8583Field.java
 * <p>
 * This class represents an ISO 8583 field entity in the database.
 *
 * @version 2.0
 */
@Getter
@Setter
@Entity
@Table(name = "iso8583_fields")
public class Iso8583Field {

    @Id
    @SequenceGenerator(
            name = "field_seq",
            sequenceName = "field_seq"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "field_seq"
    )
    private Long id;

    @Column(name = "field_id", nullable = false)
    private String fieldId;

    @Lob
    @Column(name = "field_value")
    private String fieldValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private IsoMessageType messageType;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonBackReference
    private Transaction transaction;
}
