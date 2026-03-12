package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParsedDigitalVoucherField {
    private String name;
    private int length;
    private String value;

    public ParsedDigitalVoucherField(String name, int length, String value) {
        this.name = name;
        this.length = length;
        this.value = value;
    }
}
