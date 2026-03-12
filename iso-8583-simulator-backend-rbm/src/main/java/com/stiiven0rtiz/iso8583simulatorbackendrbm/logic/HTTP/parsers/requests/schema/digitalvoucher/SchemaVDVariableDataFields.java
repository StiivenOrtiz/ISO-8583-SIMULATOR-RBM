package com.stiiven0rtiz.iso8583simulatorbackendrbm.logic.HTTP.parsers.requests.schema.digitalvoucher;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SchemaVDVariableDataFields {
    public String separatorFields;
    public String structure;
    public List<SchemaVDVariableField> fields;
}