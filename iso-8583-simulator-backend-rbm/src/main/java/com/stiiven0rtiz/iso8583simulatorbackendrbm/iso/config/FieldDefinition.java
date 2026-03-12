package com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.config;

// imports

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FieldDefinition.java
 * <p>
 * This class represents the definition of a field in the ISO8583 message.
 * Designed to be immutable after deserialization.
 *
 * @version 1.0
 */
public record FieldDefinition(String id, String name, String description, String type, String lengthType, int length,
                              String encoding) {

    /**
     * Constructor for FieldDefinition.
     *
     * @param id          The field ID.
     * @param name        The field name.
     * @param description The field description.
     * @param type        The field type.
     * @param lengthType  The length type of the field (e.g., fixed, variable).
     * @param length      The length of the field.
     * @param encoding    The encoding of the field (e.g., ASCII, EBCDIC).
     */
    @JsonCreator
    public FieldDefinition(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") String type,
            @JsonProperty("lengthType") String lengthType,
            @JsonProperty("length") int length,
            @JsonProperty("encoding") String encoding) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.lengthType = lengthType;
        this.length = length;
        this.encoding = encoding;
    }
}