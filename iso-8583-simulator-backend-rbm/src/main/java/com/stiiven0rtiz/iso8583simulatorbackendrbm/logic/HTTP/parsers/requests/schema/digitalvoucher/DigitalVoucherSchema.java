package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.schema.digitalvoucher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DigitalVoucherSchema {
    private SchemaVDField STX;
    private SchemaVDField Length;
    private SchemaVDField typeTransaction;
    private SchemaVDField startSeparator;
    private SchemaVDVariableDataFields variableDataFields;
    private SchemaVDField ETX;
    private SchemaVDField LRC;
}
