package com.stiiven0rtiz.iso8583simulatorbackend.iso.message;


// imports
import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class is used to store the lengths of the data elements in an ISO 8583 message.
 */
public class IsoDataElementsLengths {
    /**
     * The length of the secondary bitmap.
     */
    @Getter
    private final Map<Integer, ValidatedVariable<Integer>> dataElementsLengths;

    /**
     * Get the length of a data element.
     */
    public ValidatedVariable<Integer> getdataElementLength(int i){
        return dataElementsLengths.get(i);
    }

    /**
     * Set the length of a data element.
     */
    public void setdataElementLength(int i, ValidatedVariable<Integer> v){
        dataElementsLengths.put(i, v);
    }

    /**
     * Constructor for the Iso8583Lengths class.
     */
    public IsoDataElementsLengths() {
        this.dataElementsLengths = new TreeMap<>();
    }
}
