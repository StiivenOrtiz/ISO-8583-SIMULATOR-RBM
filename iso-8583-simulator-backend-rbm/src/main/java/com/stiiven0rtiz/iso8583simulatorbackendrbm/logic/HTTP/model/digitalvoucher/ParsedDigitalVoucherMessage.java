package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.model.digitalvoucher;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParsedDigitalVoucherMessage {
    private String STX;
    private int Length;
    private String typeTransaction;
    private String startSeparator;
    private List<ParsedDigitalVoucherField> variableFields = new ArrayList<>();
    private String ETX;
    private String LRC;
}
