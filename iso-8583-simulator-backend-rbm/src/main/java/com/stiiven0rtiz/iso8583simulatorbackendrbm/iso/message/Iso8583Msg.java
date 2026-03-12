package com.stiiven0rtiz.iso8583simulatorbackendrbm.iso.message;


// imports
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents an ISO 8583 message.
 *
 * This class contains the raw data of the message, as well as the TPDU, MTI, bitmap, and data elements.
 *
 * @version 1.0
 */
public class Iso8583Msg {
    /**
     * The raw data of the message.
     */
    @Getter @Setter
    private String rawData;
    /**
     * The TPDU (Transport Protocol Data Unit) of the message.
     */
    @Getter @Setter
    private ValidatedVariable<String> TPDU;
    /**
     * The MTI (Message Type Indicator) of the message.
     */
    @Getter @Setter
    private ValidatedVariable<String> MTI;
    /**
     * The bitmap of the message.
     */
    @Getter @Setter
    private ValidatedVariable<String> bitmap;
    /**
     * The data elements of the message.
     */
    @Getter
    private final Map<String, ValidatedVariable<Object>> dataElements;

    /**
     * Constructor for Iso8583Msg.
     * Initializes the TPDU, MTI, bitmap, and data elements.
     */
    public Iso8583Msg() {
        this.TPDU = new ValidatedVariable<>();
        this.MTI = new ValidatedVariable<>();
        this.bitmap = new ValidatedVariable<>();
        this.dataElements = new TreeMap<>(
                Comparator.comparingInt(s -> Integer.parseInt(s.substring(1))));
    }

    /**
     * Gets the data element for the given FiledID.
     *
     * @param fieldId The field ID of the data element.
     * @return The data element for the given field ID.
     */
    public ValidatedVariable<Object> getValidatedDataElement(String fieldId) {
        ValidatedVariable<Object> validatedDataElement = new ValidatedVariable<>();
        if (dataElements.containsKey(fieldId))
            validatedDataElement = dataElements.get(fieldId);
        return validatedDataElement;
    }

    /**
     * Sets the data element for the given ID.
     *
     * @param fieldId The filed ID of the data element.
     * @param value The value of the data element.
     */
    public void setDataElement(String fieldId, ValidatedVariable<Object> value) {
        dataElements.put(fieldId, value);
    }

    /**
     * Gets the data element for the given ID.
     *
     * @param fieldId The field ID of the data element.
     * @return The data element for the given field ID.
     */
    public Object getDataElement(String fieldId) {
        Object value = null;
        if (dataElements.containsKey(fieldId))
            value = dataElements.get(fieldId).getValue();
        return value;
    }

    /**
     * Sets the data element for the given ID.
     *
     * @param fieldId The field ID of the data element.
     * @param value The value of the data element.
     */
    public void setDataElement(String fieldId, Object value) {
        dataElements.put(fieldId, new ValidatedVariable<>(value));
    }

    /**
     * Gets the data elements of the message.
     *
     * @param fieldId The field ID of the data element.
     * @return The data elements of the message.
     */
    public boolean hasDataElement(String fieldId) {
        return dataElements.containsKey(fieldId);
    }

    /**
     * Sets the TPDU of the message.
     *
     * @param TPDU The TPDU of the message.
     */
    public void setTPDU(String TPDU) {
        this.TPDU = new ValidatedVariable<>(TPDU);
    }

    /**
     * Sets the MTI of the message.
     *
     * @param MTI The MTI of the message.
     */
    public void setMTI(String MTI) {
        this.MTI = new ValidatedVariable<>(MTI);
    }

    /**
     * Sets the bitmap of the message.
     *
     * @param bitmap The bitmap of the message.
     */
    public void setBitmap(String bitmap) {
        this.bitmap = new ValidatedVariable<>(bitmap);
    }
}
